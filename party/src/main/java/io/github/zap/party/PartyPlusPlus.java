package io.github.zap.party;

import io.github.zap.party.party.Party;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PartyPlusPlus extends Plugin {

    /**
     * Starts tracking a party
     * @param party The party to track
     */
    void trackParty(@NotNull Party party);

    /**
     * Gets the party a player is in
     * @param player The player to check
     * @return An optional of their party
     */
    @NotNull Optional<Party> getPartyForPlayer(@NotNull OfflinePlayer player);

}