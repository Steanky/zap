package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages all party logic
 */
public class PartyManager implements Listener {

    private final BukkitScheduler bukkitScheduler;

    private final Map<UUID, Party> partyMap = new HashMap<>();

    private final Map<Party, Map<UUID, Integer>> partyInvitationMap = new HashMap<>();

    public PartyManager(@NotNull BukkitScheduler bukkitScheduler) {
        this.bukkitScheduler = bukkitScheduler;
    }

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
     * @return The party of the player
     */
    public @NotNull Optional<Party> getPartyForPlayer(@NotNull OfflinePlayer player) {
        return Optional.ofNullable(partyMap.get(player.getUniqueId()));
    }

    /**
     * Gets the player as a member of a party
     * @param player The player to get the party member of
     * @return The party member representation of the player
     */
    public @NotNull Optional<PartyMember> getPlayerAsPartyMember(@NotNull OfflinePlayer player) {
        Optional<Party> partyOptional = getPartyForPlayer(player);
        return partyOptional.flatMap(party -> party.getMember(player.getUniqueId()));
    }

    /**
     * Creates a party
     * @param owner The owner of the party
     * @return The new party
     */
    public @NotNull Party createParty(@NotNull PartyMember owner, @NotNull PartySettings partySettings) {
        return partyMap.computeIfAbsent(owner.getOfflinePlayer().getUniqueId(),
                (unused) -> new Party(owner, partySettings));
    }

    /**
     * Adds a player to a party
     * @param party The party to add the player to
     * @param player The player to add to the party
     */
    public void addPlayerToParty(@NotNull Party party, @NotNull Player player) {
        party.addMember(player);
        this.partyMap.put(player.getUniqueId(), party);

        Map<UUID, Integer> map = this.partyInvitationMap.get(party);
        if (map != null) {
            this.bukkitScheduler.cancelTask(map.remove(player.getUniqueId()));
            if (map.isEmpty()) {
                this.partyInvitationMap.remove(party);
            }
        }
    }

    /**
     * Removes a player from their party
     * @param player The player to be removed from their party
     * @param forced Whether the removal was forced
     */
    public void removePlayerFromParty(@NotNull OfflinePlayer player, boolean forced) {
        Party party = this.partyMap.get(player.getUniqueId());

        if (player.getName() != null && party != null) {
            party.removeMember(player.getUniqueId(), forced);
            this.partyMap.remove(player.getUniqueId());
        }
    }

    /**
     * Disbands a party and removes all players from it
     * @param party The party to disband
     */
    public void disbandParty(@NotNull BukkitScheduler bukkitScheduler, @NotNull Party party) {
        for (OfflinePlayer player : party.disband()) {
            this.partyMap.remove(player.getUniqueId());
        }

        Map<UUID, Integer> map = this.partyInvitationMap.get(party);
        if (map != null) {
            for (Integer task : map.values()) {
                bukkitScheduler.cancelTask(task);
            }
            if (map.isEmpty()) {
                this.partyInvitationMap.remove(party);
            }
        }
    }

    /**
     * Kicks offline players in a party
     * @param party The party kick offline players in
     */
    public void kickOffline(@NotNull Party party) {
        for (OfflinePlayer player : party.kickOffline()) {
            this.partyMap.remove(player.getUniqueId());
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

        String ownerName = Objects.toString(partyOwner.getName());
        String joinCommand = String.format("/party join %s", ownerName);

        Player onlinePartyOwner = partyOwner.getPlayer();
        Component ownerComponent = (onlinePartyOwner != null)
                ? onlinePartyOwner.displayName()
                : Component.text(ownerName, NamedTextColor.GRAY);

        Component joinCommandComponent = TextComponent.ofChildren(
                Component.text("/party join "),
                ownerComponent
        );

        Component second;
        if (partyOwner.equals(inviter)) {
            second = Component.text(" has invited you to join their party! Click", NamedTextColor.YELLOW);
        }
        else {
            second = TextComponent.ofChildren(
                    Component.text(" has invited you to join ", NamedTextColor.YELLOW),
                    ownerComponent,
                    Component.text("'s party! Click", NamedTextColor.YELLOW)
            );
        }

        invitee.sendMessage(TextComponent.ofChildren(
                inviter.displayName(),
                second,
                Component.text(" here ", NamedTextColor.RED)
                        .hoverEvent(joinCommandComponent)
                        .clickEvent(ClickEvent.runCommand(joinCommand)),
                Component.text(String.format("to join! You have %.1f seconds to accept!", expirationTime),
                                NamedTextColor.YELLOW)
        ));

        party.broadcastMessage(TextComponent.ofChildren(
                inviter.displayName(),
                Component.text(" has invited ", NamedTextColor.YELLOW),
                invitee.displayName(),
                Component.text(String.format(" to the party! They have %.1f seconds to accept.",
                        expirationTime), NamedTextColor.YELLOW)
        ));

        Component inviteeDisplayName = invitee.displayName();

        int taskId = this.bukkitScheduler.runTaskLater(PartyPlusPlus.getInstance(), () -> {
            party.removeInvite(invitee);

            if (!party.hasMember(invitee.getUniqueId())) {
                party.broadcastMessage(TextComponent.ofChildren(
                        Component.text("The invite to ", NamedTextColor.YELLOW),
                        inviteeDisplayName,
                        Component.text(" has expired.", NamedTextColor.YELLOW)
                ));

                if (invitee.isOnline()) {
                    invitee.sendMessage(TextComponent.ofChildren(
                            Component.text("The invite to ", NamedTextColor.YELLOW),
                            ownerComponent,
                            Component.text("'s party has expired.")
                    ));
                }
            }
        }, party.getPartySettings().getInviteExpirationTime()).getTaskId();
        this.partyInvitationMap.computeIfAbsent(party, unused -> new HashMap<>()).put(invitee.getUniqueId(), taskId);
    }

    @EventHandler
    private void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Optional<Party> partyOptional = this.getPartyForPlayer(player);

        if (partyOptional.isPresent()) {
            Party party = partyOptional.get();
            Optional<PartyMember> optionalPartyMember = party.getMember(player.getUniqueId());
            if (optionalPartyMember.isPresent()) {
                PartyMember partyMember = optionalPartyMember.get();
                if (partyMember.isInPartyChat()) {
                    event.setCancelled(true);

                    if (partyMember.isMuted()) {
                        player.sendMessage(Component.text("You are muted from speaking in the party chat.",
                                NamedTextColor.RED));
                    }
                    else if (party.getPartySettings().isMuted() && !party.isOwner(player)) {
                        player.sendMessage(Component.text("The party chat is muted.", NamedTextColor.RED));
                    }
                    else {
                        party.broadcastMessage(TextComponent.ofChildren(
                                Component.text("Party", NamedTextColor.BLUE),
                                Component.text(" >", NamedTextColor.DARK_GRAY),
                                Component.text("<", NamedTextColor.WHITE),
                                event.getPlayer().displayName(),
                                Component.text("> ")
                        ));
                    }
                }
            }
        }
    }

}
