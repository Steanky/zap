package io.github.zap.arenaapi.localization;

import com.google.common.io.Files;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.playerdata.PlayerData;
import io.github.zap.arenaapi.playerdata.PlayerDataManager;
import io.github.zap.arenaapi.util.FileUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;

public class LocalizationManager {
    private static final String EXTENSION = "lang";

    private final Map<Locale, Translation> resources = new HashMap<>();
    private final Locale defaultLocale;
    private final File localizationFileDirectory;
    private final PlayerDataManager dataManager;

    public LocalizationManager(Locale defaultLocale, File localizationFileDirectory, PlayerDataManager dataManager) throws LoadFailureException {
        this.defaultLocale = defaultLocale;
        this.localizationFileDirectory = localizationFileDirectory;

        try {
            if (!localizationFileDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                localizationFileDirectory.mkdirs();
            }
        } catch (SecurityException e) {
            throw new LoadFailureException("SecurityException when creating localization file folder.");
        }

        this.dataManager = dataManager;
        loadTranslations();
    }

    public String getLocalizedMessage(Locale locale, String messageKey) {
        Translation translation = resources.get(locale);

        if (translation == null) {
            translation = resources.get(defaultLocale);

            if(translation == null) {
                return String.format("Missing translation for locale %s and key %s. Report this error to server" +
                        " administration.", locale.toLanguageTag(), messageKey);
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
        FileUtils.forEachFile(localizationFileDirectory, (file) -> {
            if(file.isFile() && Files.getFileExtension(file.getName()).equals(EXTENSION)) {
                try {
                    Translation translation = TranslationParser.load(file);

                    if(translation != null) {
                        resources.put(translation.getLocale(), translation);
                        ArenaApi.info(String.format("Loaded '%s' translations.", translation.getLocale()
                                .toLanguageTag()));
                    }
                    else {
                        ArenaApi.warning(String.format("Unable to find translations in file '%s'.", file.getPath()));
                    }
                } catch (IOException e) {
                    ArenaApi.warning(String.format("Unable to load translation from file '%s': %s.", file.getPath(),
                            e.getMessage()));
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
            ArenaApi.warning(String.format("Unable to save translation to file '%s': %s", file.getPath(),
                    e.getMessage()));
        }
    }

    public void sendLocalizedMessage(Player player, String messageKey, Object ... formatArguments) {
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());

        if(data != null) {
            Locale playerLocale = Locale.forLanguageTag(data.getLocale());
            String localizedMessage = getLocalizedMessage(playerLocale, messageKey);

            if(formatArguments.length > 0) {
                try {
                    player.sendRawMessage(String.format(playerLocale, localizedMessage, formatArguments));
                }
                catch(IllegalFormatException exception) {
                    ArenaApi.warning(String.format("Illegal format string %s for MessageKey %s", localizedMessage,
                            messageKey));
                }
            }
            else {
                player.sendRawMessage(getLocalizedMessage(playerLocale, messageKey));
            }
        }
        else {
            ArenaApi.warning(String.format("An attempt was made to send a localized message to a player for whom no " +
                    "PlayerData object could be retrieved: player UUID %s, messageKey %s",
                    player.getUniqueId().toString(), messageKey));
        }
    }

    public Locale getPlayerLocale(Player player) {
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());

        if (data != null) {
            return Locale.forLanguageTag(data.getLocale());
        } else {
            ArenaApi.warning(String.format("An attempt was made to get the locale of a player for whom no PlayerData object could be retrieved: player UUID %s", player.getUniqueId().toString()));
            return null;
        }
    }

}
