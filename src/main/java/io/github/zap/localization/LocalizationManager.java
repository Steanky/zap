package io.github.zap.localization;

import com.google.common.io.Files;
import io.github.zap.ZombiesPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationManager {
    private static final String extension = "lang";

    private final Map<Locale, Translation> resources = new HashMap<>();
    private final Locale defaultLocale;
    private final File localizationFileDirectory;

    public LocalizationManager(Locale defaultLocale, File localizationFileDirectory) {
        this.defaultLocale = defaultLocale;
        this.localizationFileDirectory = localizationFileDirectory;

        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        try {
            if (!localizationFileDirectory.exists() && !localizationFileDirectory.mkdirs()) {
                zombiesPlugin.getLogger().warning(String.format("Error creating localization file directory at %s", localizationFileDirectory.getPath()));
            }
        } catch (SecurityException e) {
            zombiesPlugin.getLogger().warning(String.format("SecurityException when attemping to create translations " +
                    "folder: %s", e.getMessage()));
        }
    }

    public String getLocalizedMessage(Locale locale, MessageKey messageKey) {
        Translation translation = resources.get(locale);
        if (translation == null) {
            translation = resources.get(defaultLocale);
        }

        Map<String, String> messages = translation.getMappings();

        //this doesn't use getOrDefault to avoid evaluating String.format when unnecessary
        String message = messages.get(messageKey.getResourceKey());
        if(message == null) {
            message = String.format("Missing translation for locale %s and MessageKey %s. Report this error to server" +
                    " administration.", locale.toLanguageTag(), messageKey);
        }

        return message;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void loadTranslations() {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();

        File[] files = localizationFileDirectory.listFiles();

        if(files != null) {
            for(File file : files) {
                if(file.isFile() && Files.getFileExtension(file.getName()).equals(extension)) {
                    try {
                        Translation translation = TranslationParser.load(file);

                        if(translation != null) {
                            resources.put(translation.getLocale(), translation);
                            zombiesPlugin.getLogger().info(String.format("Loaded '%s' translations",
                                    translation.getLocale().toLanguageTag()));
                        }
                        else {
                            zombiesPlugin.getLogger().warning(String.format("Unable to find translations " +
                                    "in file '%s", file.getPath()));
                        }
                    } catch (IOException e) {
                        zombiesPlugin.getLogger().warning(String.format("Unable to load translation from file " +
                                "'%s': %s", file.getPath(), e.getMessage()));
                    }
                }
            }
        }
        else {
            zombiesPlugin.getLogger().warning("No translations loaded");
        }
    }

    public void saveTranslation(Translation translation) {
        File file = Paths.get(localizationFileDirectory.getPath(), String.format("%s.%s", translation.getFileName(), extension)).toFile();
        try {
            TranslationParser.save(file, translation);
        } catch (IOException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Unable to load translation to file " +
                    "'%s': %s", file.getPath(), e.getMessage()));
        }
    }
}