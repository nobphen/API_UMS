import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares the newest official country list (ISO 3166-1) with the countries table and writes a Flyway migration
 * with only the differences. It never changes the database itself: commit the file and deploy.
 *
 * Run from the project root:   ./gradlew countrySync                    (latest country-list / Unicode CLDR names)
 *                              ./gradlew countrySync -Psource=D:/countries.csv
 *                              ./gradlew countrySync -PdryRun
 *
 * Sources: "cldr" (default, github.com/umpirsky/country-list, MIT, built from Unicode CLDR: English and Khmer names),
 *          or a CSV with columns code,name_en,name_kh[,nationality_en,nationality_kh] (header row).
 * Nationalities are not in the CLDR lists, so existing ones are kept; a new country takes them from the CSV,
 * or is reported so someone fills them in.
 * Database: DB_URL, DB_USERNAME, DB_PASSWORD (default: the local dev database).
 */
public class CountrySync {

    static final String CLDR = "https://raw.githubusercontent.com/umpirsky/country-list/master/data/";
    static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

    record Country(String code, String nameEn, String nameKh, String nationalityEn, String nationalityKh) {}

    record Current(Country country, boolean active) {}

    public static void main(String[] args) throws Exception {
        String source = arg(args, "--source", "cldr");
        boolean dryRun = List.of(args).contains("--dry-run");
        Path outDir = Path.of(arg(args, "--out", MIGRATIONS.toString()));

        Map<String, Country> official = load(source);
        Map<String, Current> current = loadDatabase();
        long activeNow = current.values().stream().filter(Current::active).count();
        System.out.printf("Official list: %s countries from %s%nDatabase:      %s countries (%s active)%n%n",
                official.size(), source, current.size(), activeNow);
        if (official.size() < activeNow * 0.9) {
            throw new IllegalStateException("The official list has only " + official.size() + " countries against "
                    + activeNow + " active ones: it looks incomplete, nothing was written");
        }

        List<Country> added = new ArrayList<>();
        List<Country[]> renamed = new ArrayList<>();   // {old, new}
        List<Country> restored = new ArrayList<>();
        List<Country> retired = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Country c : official.values()) {
            Current now = current.get(c.code);
            if (now == null) {
                added.add(c);
                if (c.nationalityEn == null) warnings.add(c.code + " " + c.nameEn + " is new: add its nationality (Khmer and English) afterwards");
                continue;
            }
            // CLDR has no nationalities: keep what the database has unless the CSV gives new ones
            Country merged = new Country(c.code, c.nameEn, c.nameKh,
                    c.nationalityEn != null ? c.nationalityEn : now.country.nationalityEn,
                    c.nationalityKh != null ? c.nationalityKh : now.country.nationalityKh);
            if (!same(now.country, merged)) renamed.add(new Country[] {now.country, merged});
            if (!now.active) restored.add(merged);
        }
        for (Current now : current.values()) {
            if (now.active && !official.containsKey(now.country.code)) retired.add(now.country);
        }
        Map<String, Integer> use = usage(retired);

        System.out.printf("New:      %d%nRenamed:  %d%nRestored: %d%nRetired:  %d%n", added.size(), renamed.size(), restored.size(), retired.size());
        show("New", added.stream().map(c -> c.code + "  " + c.nameKh + " / " + c.nameEn).toList());
        show("Renamed", renamed.stream().map(r -> r[0].code + "  " + r[0].nameKh + " / " + r[0].nameEn + "  ->  " + r[1].nameKh + " / " + r[1].nameEn).toList());
        show("Restored", restored.stream().map(c -> c.code + "  " + c.nameEn).toList());
        show("Retired (switched off, never deleted)", retired.stream().map(c -> c.code + "  " + c.nameKh + " / " + c.nameEn
                + (use.containsKey(c.code) ? "   <- used by " + use.get(c.code) + " person(s) or address(es)" : "")).toList());
        show("Warnings", warnings);
        System.out.println();

        if (added.isEmpty() && renamed.isEmpty() && restored.isEmpty() && retired.isEmpty()) {
            System.out.println("countries is already up to date: no migration written.");
            return;
        }
        if (dryRun) {
            System.out.println("Dry run: no migration written.");
            return;
        }
        Path file = outDir.resolve("V" + nextVersion() + "__update_countries_"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".sql");
        Files.createDirectories(outDir);
        Files.writeString(file, migration(source, added, renamed, restored, retired), StandardCharsets.UTF_8);
        System.out.println("Written: " + file + "\nCheck it, commit it, and deploy: Flyway applies it once on every server.");
    }

    /// LOAD -------------------------------------------------------------

    static Map<String, Country> load(String source) throws IOException, InterruptedException {
        Map<String, Country> out = new TreeMap<>();
        if (source.toLowerCase().endsWith(".csv")) {
            List<String> lines = Files.readAllLines(Path.of(source), StandardCharsets.UTF_8);
            for (String line : lines.subList(1, lines.size())) {
                if (line.isBlank()) continue;
                List<String> f = csv(line.replace("\uFEFF", ""));
                String code = f.get(0).trim().toUpperCase();
                checkCode(code);
                out.put(code, new Country(code, clean(f.get(1)), clean(f.get(2)),
                        f.size() > 3 ? blankToNull(clean(f.get(3))) : null, f.size() > 4 ? blankToNull(clean(f.get(4))) : null));
            }
            return out;
        }
        Map<String, String> en = cldr(download(CLDR + "en/country.csv"));
        Map<String, String> km = cldr(download(CLDR + "km/country.csv"));
        for (String code : en.keySet()) {
            out.put(code, new Country(code, en.get(code), km.getOrDefault(code, en.get(code)), null, null));
        }
        return out;
    }

    /** country-list CSV: id,value with a header row. */
    static Map<String, String> cldr(String text) {
        Map<String, String> out = new TreeMap<>();
        String[] lines = text.split("\r?\n");
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            List<String> f = csv(lines[i]);
            checkCode(f.get(0));
            out.put(f.get(0), clean(f.get(1)));
        }
        return out;
    }

    static Map<String, Current> loadDatabase() throws Exception {
        Map<String, Current> out = new TreeMap<>();
        try (Connection db = connect(); ResultSet rs = db.createStatement().executeQuery(
                "select code, name_en, name_kh, nationality_en, nationality_kh, is_active from countries")) {
            while (rs.next()) {
                Country c = new Country(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                out.put(c.code, new Current(c, rs.getBoolean(6)));
            }
        }
        return out;
    }

    /** People with that nationality plus addresses in that country, for each country about to be retired. */
    static Map<String, Integer> usage(List<Country> retired) throws Exception {
        Map<String, Integer> use = new TreeMap<>();
        if (retired.isEmpty()) return use;
        try (Connection db = connect(); ResultSet rs = db.createStatement().executeQuery("""
                select code, count(*) from (
                    select nationality_code as code from persons
                    union all select country_code from person_addresses) u
                group by code""")) {
            while (rs.next()) use.put(rs.getString(1), rs.getInt(2));
        }
        use.keySet().retainAll(retired.stream().map(Country::code).toList());
        return use;
    }

    static Connection connect() throws Exception {
        return DriverManager.getConnection(
                env("DB_URL", "jdbc:postgresql://localhost:5432/ums"), env("DB_USERNAME", "ums"), env("DB_PASSWORD", "ums"));
    }

    /// MIGRATION ----------------------------------------------------------

    static String migration(String source, List<Country> added, List<Country[]> renamed, List<Country> restored, List<Country> retired) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- =====================================================================\n")
           .append("-- countries update generated by tools/CountrySync.java on ").append(LocalDate.now()).append('\n')
           .append("-- Source: ").append(source.equals("cldr") ? "country-list, github.com/umpirsky/country-list (MIT, Unicode CLDR)" : source).append('\n')
           .append("-- New ").append(added.size()).append(", renamed ").append(renamed.size()).append(", restored ")
           .append(restored.size()).append(", retired ").append(retired.size()).append(".\n")
           .append("-- Retired countries are switched off, never deleted: people and addresses keep showing them.\n")
           .append("-- =====================================================================\n");
        if (!added.isEmpty()) {
            sql.append("\n-- New countries\n");
            for (Country c : added) {
                sql.append("INSERT INTO countries (code, name_en, name_kh, nationality_en, nationality_kh) VALUES (")
                   .append(String.join(", ", q(c.code), q(c.nameEn), q(c.nameKh), q(c.nationalityEn), q(c.nationalityKh)))
                   .append(") ON CONFLICT (code) DO NOTHING;\n");
            }
        }
        if (!renamed.isEmpty()) {
            sql.append("\n-- Renamed countries (same code)\n");
            for (Country[] r : renamed) {
                Country c = r[1];
                sql.append("UPDATE countries SET name_en = ").append(q(c.nameEn)).append(", name_kh = ").append(q(c.nameKh))
                   .append(", nationality_en = ").append(q(c.nationalityEn)).append(", nationality_kh = ").append(q(c.nationalityKh))
                   .append(" WHERE code = ").append(q(c.code)).append(";   -- was ").append(r[0].nameEn).append('\n');
            }
        }
        if (!restored.isEmpty()) {
            sql.append("\n-- Countries back in the official list\n")
               .append("UPDATE countries SET is_active = true WHERE code IN (").append(codes(restored)).append(");\n");
        }
        if (!retired.isEmpty()) {
            sql.append("\n-- Countries no longer in the official list: switched off, never deleted\n")
               .append("UPDATE countries SET is_active = false WHERE code IN (").append(codes(retired)).append(");\n");
        }
        return sql.toString();
    }

    /// HELPERS -------------------------------------------------------------

    /** The same cleaning as V5: no invisible characters, single spaces, trimmed. */
    static String clean(String s) {
        if (s == null) return null;
        return s.replaceAll("[\\u200B\\u200C\\uFEFF]", "").replace('\u00A0', ' ').replaceAll("\\s{2,}", " ").trim();
    }

    static String blankToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    static boolean same(Country a, Country b) {
        return Objects.equals(a.nameEn, b.nameEn) && Objects.equals(a.nameKh, b.nameKh)
                && Objects.equals(a.nationalityEn, b.nationalityEn) && Objects.equals(a.nationalityKh, b.nationalityKh);
    }

    static void checkCode(String code) {
        if (!code.matches("[A-Z]{2}")) throw new IllegalArgumentException("Not an ISO 3166-1 alpha-2 code: " + code);
    }

    static void show(String title, List<String> lines) {
        if (lines.isEmpty()) return;
        System.out.println("\n" + title + ":");
        lines.forEach(l -> System.out.println("  " + l));
    }

    static int nextVersion() throws IOException {
        int max = 0;
        try (var files = Files.list(MIGRATIONS)) {
            for (Path p : files.toList()) {
                Matcher m = Pattern.compile("^V(\\d+)__").matcher(p.getFileName().toString());
                if (m.find()) max = Math.max(max, Integer.parseInt(m.group(1)));
            }
        }
        return max + 1;
    }

    static String codes(List<Country> list) {
        return String.join(", ", list.stream().map(c -> q(c.code)).toList());
    }

    static String q(String v) {
        return v == null ? "NULL" : "'" + v.replace("'", "''") + "'";
    }

    static List<String> csv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else if (c == '"') quoted = false;
                else cur.append(c);
            } else if (c == '"') quoted = true;
            else if (c == ',') { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }

    static String download(String url) throws IOException, InterruptedException {
        HttpResponse<String> r = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (r.statusCode() != 200) throw new IOException("Download failed (" + r.statusCode() + "): " + url);
        return r.body();
    }

    static String arg(String[] args, String name, String fallback) {
        for (int i = 0; i < args.length - 1; i++) if (args[i].equals(name)) return args[i + 1];
        return fallback;
    }

    static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? fallback : v;
    }
}
