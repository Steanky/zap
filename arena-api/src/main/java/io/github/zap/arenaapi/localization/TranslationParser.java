package io.github.zap.arenaapi.localization;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TranslationParser {
    private static final String LOCALE = "locale";

    public static void save(File file, Translation translation) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.println(String.format("%s=%s", LOCALE, translation.getLocale().toLanguageTag()));

        for (Map.Entry<String, String> mapping : translation.getMappings().entrySet()) {
            printWriter.println(String.format("%s=%s", mapping.getKey().trim(), mapping.getValue().trim()));
        }

        printWriter.close();
    }

    public static Translation load(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Map<String, String> values = new HashMap<>();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] parts = line.split("=", 2);

            if (parts.length >= 2) {
                values.put(parts[0].trim(), parts[1].trim());
            }
        }

        bufferedReader.close();

        Locale locale = Locale.forLanguageTag(values.remove(LOCALE));

        if (locale == null) {
            return null;
        }

        Translation translation = new Translation(locale, file.getName());
        translation.getMappings().putAll(values);

        return translation;
    }
}
