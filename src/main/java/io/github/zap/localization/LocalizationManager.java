package io.github.zap.localization;

import com.google.common.io.Files;
import io.github.zap.ZombiesPlugin;
import io.github.zap.serialize.DataLoader;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class LocalizationManager {
    private static final String extension = "yml";
    private static final String dataName = "translations";

    private final Map<Locale, Translation> resources = new HashMap<>();
    private final Locale defaultLocale;
    private final File localizationFileDirectory;

    public String getLocalizedMessage(Locale locale, MessageKey messageKey) {
        Map<String, String> messages = resources.get(locale).getMappings();
        if(messages == null) {
            messages = resources.get(defaultLocale).getMappings();
        }

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
        DataLoader loader = zombiesPlugin.getDataLoader();

        try {
            //noinspection ResultOfMethodCallIgnored
            localizationFileDirectory.mkdir();

            File[] files = localizationFileDirectory.listFiles();

            if(files != null) {
                for(File file : files) { //TODO: make custom lang file parser instead of using DataSerializable
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

        loader.save(translation, Paths.get(localizationFileDirectory.getPath(),
                translation.getLocale().toString() + "." + extension).toFile(), dataName);
    }
}