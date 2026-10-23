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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares the newest official list of provinces, districts, communes and villages with geo_areas and writes
 * a Flyway migration with only the differences. It never changes the database itself: commit the file and
 * deploy, and every server (SIT, UAT, PROD) applies the same update once.
 *
 * Run from the project root:   ./gradlew geoSync                        (latest Pumi data from GitHub)
 *                              ./gradlew geoSync -Psource=D:/new-gazetteer.csv
 *                              ./gradlew geoSync -Psource=D:/pumi-data -PdryRun
 *
 * Sources: "pumi" (default, github.com/dwilkie/pumi, which follows the Ministry of Interior's gazetteer),
 *          a folder with Pumi's provinces/districts/communes/villages.yml, or a CSV saved from the ministry's
 *          Excel with columns code,name_kh,name_en,unit_kh,unit_en (header row; the level comes from the code length).
 * Database: DB_URL, DB_USERNAME, DB_PASSWORD (default: the local dev database). Point it at a read-only copy of
 *          production to compare against what production really has, including areas admins added by hand.
 */
public class GeoSync {

    static final String PUMI = "https://raw.githubusercontent.com/dwilkie/pumi/master/data/";
    static final String[] LEVELS = {"PROVINCE", "DISTRICT", "COMMUNE", "VILLAGE"};
    static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

    record Area(String code, String nameKh, String nameEn, String unitKh, String unitEn) {
        String level() { return LEVELS[code.length() / 2 - 1]; }
        String parentCode() { return code.length() == 2 ? null : code.substring(0, code.length() - 2); }
    }

    record Current(Area area, boolean active) {}

    public static void main(String[] args) throws Exception {
        String source = arg(args, "--source", "pumi");
        boolean dryRun = List.of(args).contains("--dry-run");
        Path outDir = Path.of(arg(args, "--out", MIGRATIONS.toString()));

        Map<String, Area> official = load(source);
        Map<String, Current> current = loadDatabase();
        System.out.printf("Official list: %s areas from %s%nDatabase:      %s areas (%s active)%n%n",
                official.size(), source, current.size(), current.values().stream().filter(Current::active).count());

        // A broken download or a half file must never switch off half the country
        long activeNow = current.values().stream().filter(Current::active).count();
        if (official.size() < activeNow * 0.9) {
            throw new IllegalStateException("The official list has only " + official.size() + " areas against "
                    + activeNow + " active ones: it looks incomplete, nothing was written");
        }

        List<Area> added = new ArrayList<>();
        List<Area[]> renamed = new ArrayList<>();      // {old, new}
        List<Area> restored = new ArrayList<>();
        List<Area> abolished = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Area a : official.values()) {
            if (a.code.length() > 2 && !official.containsKey(a.parentCode())) {
                warnings.add(a.code + " " + a.nameEn + ": parent " + a.parentCode() + " is not in the official list (skipped)");
                continue;
            }
            if (a.nameEn.isEmpty() || a.nameKh.isEmpty()) {
                warnings.add(a.code + " has no " + (a.nameKh.isEmpty() ? "Khmer" : "English") + " name in the official list");
            }
            Current c = current.get(a.code);
            if (c == null) {
                added.add(a);
                continue;
            }
            if (!sameNames(c.area, a)) renamed.add(new Area[] {c.area, a});
            if (!c.active) restored.add(a);
        }
        for (Current c : current.values()) {
            if (c.active && !official.containsKey(c.area.code)) abolished.add(c.area);
        }
        Map<String, Integer> addressUse = addressUse(abolished);

        report(added, renamed, restored, abolished, addressUse, warnings);
        if (added.isEmpty() && renamed.isEmpty() && restored.isEmpty() && abolished.isEmpty()) {
            System.out.println("geo_areas is already up to date: no migration written.");
            return;
        }
        if (dryRun) {
            System.out.println("Dry run: no migration written.");
            return;
        }
        Path file = outDir.resolve("V" + nextVersion() + "__update_geo_areas_"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".sql");
        Files.createDirectories(outDir);
        Files.writeString(file, migration(source, added, renamed, restored, abolished), StandardCharsets.UTF_8);
        System.out.println("Written: " + file + "\nCheck it, commit it, and deploy: Flyway applies it once on every server.");
    }

    /// LOAD THE OFFICIAL LIST ------------------------------------------

    static Map<String, Area> load(String source) throws IOException, InterruptedException {
        Map<String, Area> areas = new TreeMap<>();   // sorted by code, so parents come before children
        if (source.toLowerCase().endsWith(".csv")) {
            List<String> lines = Files.readAllLines(Path.of(source), StandardCharsets.UTF_8);
            for (String line : lines.subList(1, lines.size())) {
                if (line.isBlank()) continue;
                List<String> f = csv(line.replace("\uFEFF", ""));
                Area a = new Area(f.get(0).trim(), clean(f.get(1)), clean(f.get(2)),
                        f.size() > 3 ? clean(f.get(3)) : null, f.size() > 4 ? clean(f.get(4)) : null);
                checkCode(a.code);
                areas.put(a.code, a);
            }
            return areas;
        }
        for (String file : new String[] {"provinces", "districts", "communes", "villages"}) {
            String yaml = source.equals("pumi") ? download(PUMI + file + ".yml")
                    : Files.readString(Path.of(source, file + ".yml"), StandardCharsets.UTF_8);
            areas.putAll(parsePumiYaml(yaml));
        }
        return areas;
    }

    /** Pumi's YAML: "  'code':" then "name:" and "administrative_unit:" blocks with km/latin/en lines. */
    static Map<String, Area> parsePumiYaml(String yaml) {
        Map<String, Area> out = new LinkedHashMap<>();
        Pattern record = Pattern.compile("^  '?(\\d+)'?:\\s*$");
        Pattern section = Pattern.compile("^    ([a-z_0-9]+):\\s*$");
        Pattern value = Pattern.compile("^      (km|latin|en):\\s*(.+)$");
        String code = null, sec = "", kh = null, en = null, ukh = null, uen = null;
        for (String line : (yaml + "\n  '0':").split("\r?\n")) {
            Matcher m;
            if ((m = record.matcher(line)).matches()) {
                if (code != null) out.put(code, new Area(code, clean(kh), clean(en), clean(ukh), clean(uen)));
                code = m.group(1);
                sec = "";
                kh = en = ukh = uen = null;
            } else if ((m = section.matcher(line)).matches()) {
                sec = m.group(1);
            } else if (line.startsWith("    ") && !line.startsWith("     ")) {
                sec = "";
            } else if ((m = value.matcher(line)).matches()) {
                String v = unquote(m.group(2));
                switch (sec + "." + m.group(1)) {
                    case "name.km" -> kh = v;
                    case "name.latin" -> en = v;
                    case "administrative_unit.km" -> ukh = v;
                    case "administrative_unit.en" -> uen = v;
                    default -> { }
                }
            }
        }
        out.remove("0");
        return out;
    }

    /// LOAD THE DATABASE ------------------------------------------------

    static Map<String, Current> loadDatabase() throws Exception {
        Map<String, Current> out = new TreeMap<>();
        try (Connection db = connect(); ResultSet rs = db.createStatement().executeQuery(
                "select code, name_kh, name_en, unit_kh, unit_en, is_active from geo_areas")) {
            while (rs.next()) {
                Area a = new Area(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                out.put(a.code, new Current(a, rs.getBoolean(6)));
            }
        }
        return out;
    }

    /** How many person addresses point to each area that is about to be switched off. */
    static Map<String, Integer> addressUse(List<Area> abolished) throws Exception {
        Map<String, Integer> use = new TreeMap<>();
        if (abolished.isEmpty()) return use;
        try (Connection db = connect(); ResultSet rs = db.createStatement().executeQuery("""
                select g.code, count(*) from person_addresses a
                join geo_areas g on g.id in (a.village_id, a.commune_id, a.district_id, a.province_id)
                group by g.code""")) {
            while (rs.next()) use.put(rs.getString(1), rs.getInt(2));
        }
        use.keySet().retainAll(abolished.stream().map(Area::code).toList());
        return use;
    }

    static Connection connect() throws Exception {
        return DriverManager.getConnection(
                env("DB_URL", "jdbc:postgresql://localhost:5432/ums"), env("DB_USERNAME", "ums"), env("DB_PASSWORD", "ums"));
    }

    /// REPORT AND MIGRATION --------------------------------------------

    static void report(List<Area> added, List<Area[]> renamed, List<Area> restored, List<Area> abolished,
                       Map<String, Integer> addressUse, List<String> warnings) {
        System.out.printf("New:       %d%nRenamed:   %d%nRestored:  %d%nAbolished: %d%n", added.size(), renamed.size(), restored.size(), abolished.size());
        show("New", added.stream().map(a -> a.code + "  " + a.level().toLowerCase() + "  " + a.nameKh + " / " + a.nameEn).toList());
        show("Renamed", renamed.stream().map(r -> r[0].code + "  " + r[0].nameKh + " / " + r[0].nameEn + "  ->  " + r[1].nameKh + " / " + r[1].nameEn).toList());
        show("Restored", restored.stream().map(a -> a.code + "  " + a.nameKh + " / " + a.nameEn).toList());
        show("Abolished (switched off, never deleted)", abolished.stream().map(a -> a.code + "  " + a.nameKh + " / " + a.nameEn
                + (addressUse.containsKey(a.code) ? "   <- used by " + addressUse.get(a.code) + " address(es)" : "")).toList());
        show("Warnings", warnings);
        System.out.println();
    }

    static void show(String title, List<String> lines) {
        if (lines.isEmpty()) return;
        System.out.println("\n" + title + ":");
        lines.stream().limit(50).forEach(l -> System.out.println("  " + l));
        if (lines.size() > 50) System.out.println("  … and " + (lines.size() - 50) + " more (all are in the migration)");
    }

    static String migration(String source, List<Area> added, List<Area[]> renamed, List<Area> restored, List<Area> abolished) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- =====================================================================\n")
           .append("-- geo_areas update generated by tools/GeoSync.java on ").append(LocalDate.now()).append('\n')
           .append("-- Source: ").append(source.equals("pumi") ? "Pumi gazetteer, github.com/dwilkie/pumi (MIT)" : source).append('\n')
           .append("-- New ").append(added.size()).append(", renamed ").append(renamed.size()).append(", restored ")
           .append(restored.size()).append(", abolished ").append(abolished.size()).append(".\n")
           .append("-- Abolished areas are switched off, never deleted: old addresses keep showing them.\n")
           .append("-- =====================================================================\n");

        if (!added.isEmpty()) {
            sql.append("\n-- New areas (parents first; the parent is found by its code)\n");
            for (Area a : added) {
                String values = String.join(", ", q(a.code), q(a.level()), q(a.nameKh), q(a.nameEn), q(a.unitKh), q(a.unitEn));
                if (a.parentCode() == null) {
                    sql.append("INSERT INTO geo_areas (code, level, name_kh, name_en, unit_kh, unit_en) VALUES (")
                       .append(values).append(") ON CONFLICT (code) DO NOTHING;\n");
                } else {
                    sql.append("INSERT INTO geo_areas (code, level, parent_id, name_kh, name_en, unit_kh, unit_en) SELECT ")
                       .append(q(a.code)).append(", ").append(q(a.level())).append(", p.id, ")
                       .append(String.join(", ", q(a.nameKh), q(a.nameEn), q(a.unitKh), q(a.unitEn)))
                       .append(" FROM geo_areas p WHERE p.code = ").append(q(a.parentCode())).append(" ON CONFLICT (code) DO NOTHING;\n");
                }
            }
        }
        if (!renamed.isEmpty()) {
            sql.append("\n-- Renamed areas (same code, new name)\n");
            for (Area[] r : renamed) {
                Area a = r[1];
                sql.append("UPDATE geo_areas SET name_kh = ").append(q(a.nameKh)).append(", name_en = ").append(q(a.nameEn))
                   .append(", unit_kh = ").append(q(a.unitKh)).append(", unit_en = ").append(q(a.unitEn))
                   .append(" WHERE code = ").append(q(a.code)).append(";   -- was ").append(r[0].nameEn).append('\n');
            }
        }
        if (!restored.isEmpty()) {
            sql.append("\n-- Areas back in the official list\n")
               .append("UPDATE geo_areas SET is_active = true WHERE code IN (").append(codes(restored)).append(");\n");
        }
        if (!abolished.isEmpty()) {
            sql.append("\n-- Areas no longer in the official list: switched off, never deleted\n")
               .append("UPDATE geo_areas SET is_active = false WHERE code IN (").append(codes(abolished)).append(");\n");
        }
        return sql.toString();
    }

    /// HELPERS -----------------------------------------------------------

    /** The same cleaning as V4: no invisible characters, single spaces, trimmed. */
    static String clean(String s) {
        if (s == null) return null;
        return s.replaceAll("[\\u200B\\u200C\\uFEFF]", "").replace('\u00A0', ' ').replaceAll("\\s{2,}", " ").trim();
    }

    static boolean sameNames(Area a, Area b) {
        return Objects.equals(a.nameKh, b.nameKh) && Objects.equals(a.nameEn, b.nameEn)
                && Objects.equals(a.unitKh, b.unitKh) && Objects.equals(a.unitEn, b.unitEn);
    }

    static void checkCode(String code) {
        if (!code.matches("\\d{2}|\\d{4}|\\d{6}|\\d{8}")) {
            throw new IllegalArgumentException("Not an official code (2, 4, 6 or 8 digits): " + code);
        }
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

    static String codes(List<Area> areas) {
        return String.join(", ", areas.stream().map(a -> q(a.code)).toList());
    }

    static String q(String v) {
        return v == null ? "NULL" : "'" + v.replace("'", "''") + "'";
    }

    static String unquote(String v) {
        v = v.trim();
        if (v.length() >= 2 && v.startsWith("'") && v.endsWith("'")) return v.substring(1, v.length() - 1).replace("''", "'");
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) return v.substring(1, v.length() - 1).replace("\\\"", "\"");
        return v;
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
