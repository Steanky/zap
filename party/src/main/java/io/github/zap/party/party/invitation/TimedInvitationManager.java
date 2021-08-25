package io.github.zap.party.party.invitation;

import io.github.zap.party.party.Party;
import io.github.zap.party.party.member.PartyMember;
import io.github.zap.party.party.namer.OfflinePlayerNamer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Basic {@link InvitationManager} with timeouts.
 */
public class TimedInvitationManager implements InvitationManager {

    private final Map<UUID, Integer> invitationMap = new HashMap<>();

    private final Plugin plugin;

    private final MiniMessage miniMessage;

    private final OfflinePlayerNamer playerNamer;

    /**
     * Creates a basic invitation manager
     * @param plugin The plugin that owns this {@link InvitationManager}
     * @param miniMessage A {@link MiniMessage} instance to parse messages
     * @param playerNamer A namer for invitation messages
     */
    public TimedInvitationManager(@NotNull Plugin plugin, @NotNull MiniMessage miniMessage,
                                  @NotNull OfflinePlayerNamer playerNamer) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
        this.playerNamer = playerNamer;
    }

    @Override
    public boolean hasInvitation(@NotNull OfflinePlayer player) {
        return this.invitationMap.containsKey(player.getUniqueId());
    }

    @Override
    public @NotNull Set<UUID> getInvitations() {
        return new HashSet<>(this.invitationMap.keySet());
    }

    @Override
    public void addInvitation(@NotNull Party party, @NotNull OfflinePlayer invitee, @NotNull OfflinePlayer inviter) {
        if (!party.hasMember(inviter)) {
            return;
        }

        Optional<PartyMember> ownerOptional = party.getOwner();
        if (ownerOptional.isEmpty()) {
            return;
        }

        double expirationTime = party.getPartySettings().getInviteExpirationTime() / 20F;

        OfflinePlayer partyOwner = ownerOptional.get().getOfflinePlayer();
        Player onlineInvitee = invitee.getPlayer();

        Component inviterComponent = this.playerNamer.name(inviter);
        Component inviteeComponent = this.playerNamer.name(invitee);
        Component ownerComponent = this.playerNamer.name(partyOwner);

        String ownerName = Objects.toString(partyOwner.getName());

        if (onlineInvitee != null) {
            Template ownerTemplate = Template.of("owner", ownerComponent);
            onlineInvitee.sendMessage(this.miniMessage.parse(String.format("<inviter> <reset><yellow>has " +
                            "invited you to join <owner-msg> <reset><yellow>party! Click " +
                            "<hover:show_text:'<yellow>/party join <reset><owner>'>" +
                            "<click:run_command:/party join " + ownerName + "><red>here <yellow>to join! " +
                            "You have %.1f seconds to accept!", expirationTime),
                    Template.of("inviter", inviterComponent),
                    (partyOwner.equals(inviter))
                            ? Template.of("owner-msg", "their")
                            : Template.of("owner-msg",
                            this.miniMessage.parse("<reset><owner>'s", ownerTemplate)), ownerTemplate));
        }

        party.broadcastMessage(this.miniMessage.parse(String.format("<inviter> <reset><yellow>has invited " +
                                "<reset><invitee> <reset><yellow>to the party! They have %.1f seconds to accept.",
                        expirationTime), Template.of("inviter", inviterComponent),
                Template.of("invitee", inviteeComponent)));

        int taskId = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            this.invitationMap.remove(invitee.getUniqueId());

            if (party.hasMember(invitee)) {
                return;
            }

            party.broadcastMessage(this.miniMessage.parse("<yellow>The invite to <reset><invitee> " +
                    "<reset><yellow>has expired.", Template.of("invitee", inviteeComponent)));

            if (onlineInvitee != null && onlineInvitee.isOnline()) {
                onlineInvitee.sendMessage(this.miniMessage.parse("<yellow>The invite to <reset><owner>" +
                        "<reset><yellow>'s party has expired.", Template.of("owner", ownerComponent)));
            }
        }, party.getPartySettings().getInviteExpirationTime()).getTaskId();
        this.invitationMap.put(invitee.getUniqueId(), taskId);
    }

    @Override
    public boolean removeInvitation(@NotNull OfflinePlayer player) {
        Integer taskId = this.invitationMap.remove(player.getUniqueId());
        if (taskId != null) {
            this.plugin.getServer().getScheduler().cancelTask(taskId);
            return true;
        }

        return false;
    }

    @Override
    public void cancelAllOutgoingInvitations() {
        Iterator<Integer> iterator = this.invitationMap.values().iterator();
        while (iterator.hasNext()) {
            Integer taskId = iterator.next();
            if (taskId != null) {
                this.plugin.getServer().getScheduler().cancelTask(taskId);
            }

            iterator.remove();
        }
    }

}
