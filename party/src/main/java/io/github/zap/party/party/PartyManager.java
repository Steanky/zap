package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all party logic
 */
public class PartyManager {

    private final Map<OfflinePlayer, Party> partyMap = new HashMap<>();

    /**
     * Creates a party's default settings
     * @param player The player to base settings off of with preferences
     * @return The generated party settings
     */
    public @NotNull PartySettings createPartySettings(@NotNull OfflinePlayer player) {
        return new PartySettings(); // TODO: have party settings from defaults based on player preferences
    }

    /**
     * Gets the party of a player
     * @param player The player to get the party of
     * @return The party of a player, or null if it does not exist
     */
    public @Nullable Party getPartyForPlayer(@NotNull OfflinePlayer player) {
        return partyMap.get(player);
    }

    /**
     * Creates a party
     * @param owner The owner of the party
     * @return The new party
     */
    public @NotNull Party createParty(@NotNull OfflinePlayer owner) {
        return partyMap.computeIfAbsent(owner, (unused) -> new Party(owner));
    }

    /**
     * Removes a player from their party
     * @param player The player to be removed from their party
     */
    public void removePlayerFromParty(@NotNull OfflinePlayer player) {
        Party party = partyMap.get(player);

        if (player.getName() != null && party != null) {
            party.removeMember(player.getName());
            partyMap.remove(player);
        }
    }

    /**
     * Adds a player to a party
     * @param party The party to add the player to
     * @param player The player to add to the party
     */
    public void addPlayerToParty(@NotNull Party party, @NotNull OfflinePlayer player) {
        party.addMember(player);
        partyMap.put(player, party);
    }

    /**
     * Disbands a party and removes all players from it
     * @param party The party to disband
     */
    public void disbandParty(@NotNull Party party) {
        for (OfflinePlayer player : party.disband()) {
            partyMap.remove(player);
        }
    }

    /**
     * Invites a player to a party
     * @param party The party to invite to
     * @param inviter The person who invited the player
     * @param invitee The invited player
     */
    public void invitePlayer(@NotNull Party party, @NotNull Player inviter, @NotNull Player invitee) {
        OfflinePlayer partyOwner = party.getOwner();
        party.addInvite(invitee);

        double expirationTime = party.getPartySettings().getInviteExpirationTime() / 20F;

        String ownerName = partyOwner.getName();
        String inviteeName = invitee.getName();
        String joinCommand = String.format("/party join %s", ownerName);

        Component invitation = Component.text(inviter.getName(), NamedTextColor.GRAY)
                .append(Component.text((partyOwner.equals(inviter))
                        ? " has invited you to join their party! Click"
                        : String.format(" has invited you to join %s's party! Click", ownerName),
                        NamedTextColor.YELLOW))
                .append(Component.text(" here ", NamedTextColor.RED)
                        .hoverEvent(Component.text(joinCommand, NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.runCommand(joinCommand)))
                .append(Component.text(String.format("to join! You have %.1f seconds to accept!", expirationTime),
                        NamedTextColor.YELLOW));

        invitee.sendMessage(invitation);

        Component invitationNotification = Component.text(inviter.getName(), NamedTextColor.GRAY)
                .append(Component.text(" has invited ", NamedTextColor.YELLOW))
                .append(Component.text(inviteeName, NamedTextColor.GRAY))
                .append(Component.text(String.format(" to the party! They have %.1f seconds to accept.",
                        expirationTime), NamedTextColor.YELLOW));

        party.broadcastMessage(invitationNotification);

        Bukkit.getScheduler().runTaskLater(PartyPlusPlus.getInstance(), () -> {
            party.removeInvite(invitee);

            if (!party.hasMember(invitee.getName())) {
                Component partyExpiration = Component.text("The invite to ", NamedTextColor.YELLOW)
                        .append(Component.text(inviteeName, NamedTextColor.GRAY))
                        .append(Component.text(" has expired.", NamedTextColor.YELLOW));

                party.broadcastMessage(partyExpiration);

                Component inviteeExpiration = Component.text("The invite to ", NamedTextColor.YELLOW)
                        .append(Component.text(ownerName, NamedTextColor.GRAY))
                        .append(Component.text("'s party has expired.", NamedTextColor.YELLOW));
                if (invitee.isOnline()) {
                    invitee.sendMessage(inviteeExpiration);
                }
            }
        }, party.getPartySettings().getInviteExpirationTime());
    }

}
