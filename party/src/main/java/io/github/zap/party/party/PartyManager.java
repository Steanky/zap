package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import net.kyori.adventure.text.Component;
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
     * Invites a player to a party
     * @param party The party to invite to
     * @param inviter The person who invited the player
     * @param invitee The invited player
     */
    public void invitePlayer(@NotNull Party party, @NotNull Player inviter, @NotNull Player invitee) {
        OfflinePlayer partyOwner = party.getOwner().getPlayer();

        Component inviteeComponent = Component.text(String.format("%s ", inviter.getName()), NamedTextColor.GRAY)
                .append(Component.text((partyOwner.equals(inviter))
                        ? "has invited you to join their party! Click "
                        : String.format("has invited you to join %s's party! Click ", partyOwner.getName())))
                .append(Component.text("here ", NamedTextColor.RED)
                        .hoverEvent(Component.text(String.format("/party join %s", partyOwner.getName()),
                                NamedTextColor.YELLOW)))
                .append(Component.text("to join!", NamedTextColor.YELLOW));

        invitee.sendMessage(inviteeComponent);

        Component partyMemberComponent = Component.text(String.format("%s ", inviter.getName()), NamedTextColor.GRAY)
                .append(Component.text(String.format("has invited %s to the party!", invitee.getName())));

        for (PartyMember partyMember : party.getMembers()) {
            Player player = partyMember.getPlayer().getPlayer();

            if (player != null) {
                player.sendMessage(partyMemberComponent);
            }
        }

        Bukkit.getScheduler().runTaskLater(PartyPlusPlus.getInstance(), () -> {
            party.getInvites().remove(invitee);

            if (!party.hasMember(invitee.getName())) {
                Component expirationComponent = Component.text("The invite to ", NamedTextColor.YELLOW)
                        .append(Component.text(invitee.getName(), NamedTextColor.GRAY))
                        .append(Component.text(" has expired.", NamedTextColor.YELLOW));

                for (PartyMember partyMember : party.getMembers()) {
                    Player player = partyMember.getPlayer().getPlayer();

                    if (player != null) {
                        player.sendMessage(expirationComponent);
                    }
                }
            }
        }, party.getPartySettings().getInviteExpirationTime());
    }

}
