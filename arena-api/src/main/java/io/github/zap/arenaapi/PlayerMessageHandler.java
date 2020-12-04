package io.github.zap.arenaapi;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.IllegalFormatException;
import java.util.Locale;

public final class PlayerMessageHandler {
    public static void sendLocalizedMessage(Player player, String messageKey, Object ... formatArguments) {
        ArenaApi instance = ArenaApi.getInstance();
        LocalizationManager localizationManager = instance.getLocalizationManager();
        PlayerData data = instance.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if(data != null) {
            Locale playerLocale = data.getLocale();
            String localizedMessage = localizationManager.getLocalizedMessage(playerLocale, messageKey);

            if(formatArguments.length > 0) {
                try {
                    player.sendRawMessage(String.format(localizedMessage, formatArguments));
                }
                catch(IllegalFormatException exception) {
                    ArenaApi.getInstance().getLogger().warning(String.format("Illegal format string %s for MessageKey" +
                            " %s", localizedMessage, messageKey));
                }
            }
            else {
                player.sendRawMessage(localizationManager.getLocalizedMessage(playerLocale, messageKey));
            }
        }
        else {
            ArenaApi.getInstance().getLogger().warning(String.format("An attempt was made to send a localized " +
                    "message to a player for whom no PlayerData object could be retrieved: player UUID %s, " +
                    "messageKey %s", player.getUniqueId().toString(), messageKey));
        }
    }
}