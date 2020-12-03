package io.github.zap.arenaapi.localization;

import com.google.common.io.Files;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationManager {
    private static final String EXTENSION = "lang";

    private final Map<Locale, Translation> resources = new HashMap<>();
    private final Locale defaultLocale;
    private final File localizationFileDirectory;

    public LocalizationManager(Locale defaultLocale, File localizationFileDirectory) throws LoadFailureException {
        this.defaultLocale = defaultLocale;
        this.localizationFileDirectory = localizationFileDirectory;

        ArenaApi arenaApi = ArenaApi.getInstance();

        try {
            if (!localizationFileDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                localizationFileDirectory.mkdirs();
            }
        } catch (SecurityException e) {
            throw new LoadFailureException("SecurityException when creating localization file folder");
        }
    }

    public String getLocalizedMessage(Locale locale, String messageKey) {
        Translation translation = resources.get(locale);

        if (translation == null) {
            translation = resources.get(defaultLocale);

            if(translation == null) {
                return String.format("We wanted to send you a message, but no translation for your language exists " +
                        "and no suitable default could be found (locale %s, key %s). Send this screenshot to server " +
                        "administration.", locale.toLanguageTag(), messageKey);
            }
        }

        Map<String, String> messages = translation.getMappings();

        //this doesn't use getOrDefault to avoid evaluating String.format when unnecessary
        String message = messages.get(messageKey);
        if(message == null) {
            message = String.format("Missing translation for locale %s and key %s. Report this error to server" +
                    " administration.", locale.toLanguageTag(), messageKey);
        }

        return message;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void loadTranslations() {
        ArenaApi arenaApi = ArenaApi.getInstance();

        FileUtils.forEachFile(localizationFileDirectory, (file) -> {
            if(file.isFile() && Files.getFileExtension(file.getName()).equals(EXTENSION)) {
                try {
                    Translation translation = TranslationParser.load(file);

                    if(translation != null) {
                        resources.put(translation.getLocale(), translation);
                        arenaApi.getLogger().info(String.format("Loaded '%s' translations", translation.getLocale()
                                .toLanguageTag()));
                    }
                    else {
                        arenaApi.getLogger().warning(String.format("Unable to find translations in file '%s",
                                file.getPath()));
                    }
                } catch (IOException e) {
                    arenaApi.getLogger().warning(String.format("Unable to load translation from file '%s': %s",
                            file.getPath(), e.getMessage()));
                }
            }
        });
    }

    public void saveTranslation(Translation translation) {
        File file = Paths.get(localizationFileDirectory.getPath(), String.format("%s.%s", translation.getFileName(),
                EXTENSION)).toFile();
        try {
            TranslationParser.save(file, translation);
        } catch (IOException e) {
            ArenaApi.getInstance().getLogger().warning(String.format("Unable to save translation to file '%s': %s",
                    file.getPath(), e.getMessage()));
        }
    }
}