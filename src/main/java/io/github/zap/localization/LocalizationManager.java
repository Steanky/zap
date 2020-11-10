package io.github.zap.localization;

import com.google.common.io.Files;
import io.github.zap.ZombiesPlugin;
import io.github.zap.serialize.DataLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class LocalizationManager {
    private static final String errorMessage = "Missing translation for locale %s and MessageKey %s. Report this " +
            "error to server administration.";
    private static final String extension = "lang";
    private static final String dataName = "translations";

    private final Map<Locale, Translation> resources = new HashMap<>();
    private final Locale defaultLocale;
    private final File localizationFileDirectory;

    public String getLocalizedMessage(Locale locale, MessageKey messageKey) {
        Map<MessageKey, String> messages = resources.get(locale).getTranslations();
        if(messages == null) {
            messages = resources.get(defaultLocale).getTranslations();
        }

        //this doesn't use getOrDefault to avoid evaluating String.format when unnecessary
        String message = messages.get(messageKey);
        if(message == null) {
            message = String.format(errorMessage, locale.toLanguageTag(), messageKey);
        }

        return message;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void loadTranslations() {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        DataLoader loader = zombiesPlugin.getDataLoader();

        try {
            //noinspection ResultOfMethodCallIgnored
            localizationFileDirectory.mkdir();

            File[] files = localizationFileDirectory.listFiles();

            if(files != null) {
                for(File file : files) {
                    if(file.isFile() && Files.getFileExtension(file.getName()).equals(extension)) {
                        try {
                            Translation translation = loader.load(file, dataName);
                            if(translation != null) {
                                resources.put(translation.getLocale(), translation);
                                zombiesPlugin.getLogger().info(String.format("Loaded '%s' translations",
                                        translation.getLocale().toString()));
                            }
                            else {
                                zombiesPlugin.getLogger().warning(String.format("Unable to find translations under " +
                                        "data name '%s' in file '%s", dataName, file.getPath()));
                            }
                        }
                        catch (ClassCastException e) {
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
        catch (SecurityException e) {
            zombiesPlugin.getLogger().warning(String.format("SecurityException when attemping to create translations " +
                    "folder: %s", e.getMessage()));
        }
    }

    public void saveTranslation(Translation translation) {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        DataLoader loader = zombiesPlugin.getDataLoader();

        loader.save(translation, Paths.get(localizationFileDirectory.getAbsolutePath(),
                translation.getLocale().toString()).toFile(), dataName);
    }
}