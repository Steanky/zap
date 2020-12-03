package io.github.zap.arenaapi;

import io.github.zap.arenaapi.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class PlayerMessageHandler {
    public static void sendLocalizedMessage(Player player, String messageKey) {
        ArenaApi instance = ArenaApi.getInstance();
        PlayerData data = instance.getDataManager().getPlayerData(player.getUniqueId());

        if(data != null) {
            Locale playerLocale = data.getLocale();
            player.sendRawMessage(instance.getLocalizationManager().getLocalizedMessage(playerLocale, messageKey));
        }
        else {
            ArenaApi.getInstance().getLogger().warning(String.format("An attempt was made to send a localized " +
                    "message to a player for whom no PlayerData object could be retrieved: player UUID %s, " +
                    "messageKey %s", player.getUniqueId().toString(), messageKey));
        }
    }
}