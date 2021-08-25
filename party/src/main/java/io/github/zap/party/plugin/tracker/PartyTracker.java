package io.github.zap.party.plugin.tracker;

import io.github.zap.party.party.Party;
import io.github.zap.party.party.member.PartyMember;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks parties and sends {@link AsyncChatEvent}s.
 */
public class PartyTracker implements Listener {

    private final Map<UUID, Party> partyMap = new HashMap<>();

    /**
     * Starts tracking a party
     * @param party The party to track
     */
    public void trackParty(@NotNull Party party) {
        party.registerJoinHandler(member -> {
            OfflinePlayer player = member.getOfflinePlayer();
            if (this.partyMap.containsKey(player.getUniqueId())) {
                this.partyMap.get(player.getUniqueId()).removeMember(player, false);
            }

            this.partyMap.put(member.getOfflinePlayer().getUniqueId(), party);
        });
        party.registerLeaveHandler(member -> this.partyMap.remove(member.getOfflinePlayer().getUniqueId()));

        for (PartyMember member : party.getMembers()) {
            this.partyMap.put(member.getOfflinePlayer().getUniqueId(), party);
        }
    }

    /**
     * Gets the party a player is in
     * @param player The player to check
     * @return An optional of their party
     */
    public @NotNull Optional<Party> getPartyForPlayer(@NotNull OfflinePlayer player) {
        return Optional.ofNullable(this.partyMap.get(player.getUniqueId()));
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        this.getPartyForPlayer(event.getPlayer()).ifPresent(party -> party.onAsyncChat(event));
    }

}
