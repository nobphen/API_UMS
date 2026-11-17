package com.ums.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Catches a missing message before Postman does: without its line in the .properties files,
 * the API answers with the key itself (e.g. "error.guardian.not_found") instead of the text.
 * Plain unit test: reads the source files, no Spring and no database.
 */
class MessageKeysTest {

    private static final Path SOURCE = Path.of("src/main/java");
    private static final Path EN = Path.of("src/main/resources/i18n/messages.properties");
    private static final Path KM = Path.of("src/main/resources/i18n/messages_km.properties");

    /** A key in quotes, also inside {} as in @Pattern(message = "{validation.email}"). */
    private static final Pattern KEY = Pattern.compile("\"\\{?((?:error|auth|success|validation)\\.[a-z0-9_.]+)}?\"");

    /** Every message key used in the Java code is in the English AND the Khmer file. */
    @Test
    void everyKeyInTheCodeHasAMessage() throws IOException {

        // 1. The keys used in the code, and the two message files
        Set<String> used = keysInCode();
        Properties en = load(EN);
        Properties km = load(KM);

        // 2. The ones without a line in each file
        Set<String> missingEn = new TreeSet<>();
        Set<String> missingKm = new TreeSet<>();
        for (String key : used) {
            if (!en.containsKey(key)) missingEn.add(key);
            if (!km.containsKey(key)) missingKm.add(key);
        }

        // 3. Both lists must be empty (the failure message lists the keys to add)
        assertThat(missingEn).as("keys to add to messages.properties").isEmpty();
        assertThat(missingKm).as("keys to add to messages_km.properties").isEmpty();
    }

    /** Every English message has its Khmer copy (Khmer may have more, e.g. the jakarta.validation overrides). */
    @Test
    void everyEnglishMessageHasAKhmerOne() throws IOException {

        // 1. Both files
        Properties en = load(EN);
        Properties km = load(KM);

        // 2. English keys that Khmer does not have
        Set<String> missingKm = new TreeSet<>();
        for (Object key : en.keySet()) {
            if (!km.containsKey(key)) missingKm.add((String) key);
        }

        // 3. Must be empty
        assertThat(missingKm).as("keys to add to messages_km.properties").isEmpty();
    }

    /// HELPERS ------------------------------------------------------

    /**
     * Every "error.x", "auth.x", "success.x", "validation.x" in the code. Skips comment lines
     * (an example key in a Javadoc is not a real use) and prefixes built in code ("auth.account_" + status).
     */
    private static Set<String> keysInCode() throws IOException {
        Set<String> keys = new TreeSet<>();
        try (Stream<Path> files = Files.walk(SOURCE)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("*") || trimmed.startsWith("//") || trimmed.startsWith("/*")) continue;
                    Matcher m = KEY.matcher(line);
                    while (m.find()) {
                        String key = m.group(1);
                        if (!key.endsWith(".") && !key.endsWith("_")) keys.add(key);
                    }
                }
            }
        }
        return keys;
    }

    /** One .properties file, read as UTF-8 (the Khmer text). */
    private static Properties load(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
