package io.github.zap.party.party;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A group of players which can join games together.
 */
public class Party {

    private final static Random RANDOM = new Random();

    private final UUID uuid = UUID.randomUUID();

    private final List<Consumer<PartyMember>> partyJoinHandlers = new ArrayList<>();

    private final List<Consumer<PartyMember>> partyLeaveHandlers = new ArrayList<>();

    private final Plugin plugin;

    private final PartySettings partySettings;

    private final Function<Player, PartyMember> partyMemberBuilder;

    private final Map<UUID, PartyMember> members = new HashMap<>();

    private final Map<UUID, Integer> invitationMap = new HashMap<>();

    private PartyMember owner;

    public Party(@NotNull Plugin plugin, @NotNull PartyMember owner, @NotNull PartySettings partySettings,
                 @NotNull Function<Player, PartyMember> partyMemberBuilder) {
        this.plugin = plugin;
        this.owner = owner;
        this.partySettings = partySettings;
        this.partyMemberBuilder = partyMemberBuilder;

        this.members.put(owner.getOfflinePlayer().getUniqueId(), owner);
    }

    /**
     * Called when this party should handle an {@link AsyncChatEvent}
     * @param event The event
     */
    public void onAsyncChat(AsyncChatEvent event) {
        Optional<PartyMember> optionalPartyMember = this.getMember(event.getPlayer());
        if (optionalPartyMember.isPresent()) {
            PartyMember partyMember = optionalPartyMember.get();
            if (partyMember.isInPartyChat()) {
                if (partyMember.isMuted()) {
                    event.getPlayer().sendMessage(MiniMessage.get().parse("<red>You are muted from speaking " +
                                    "in the party chat."));
                    event.setCancelled(true);
                }
                else if (this.getPartySettings().isMuted() && !this.isOwner(event.getPlayer())) {
                    event.getPlayer().sendMessage(MiniMessage.get().parse("<red>The party chat is muted."));
                    event.setCancelled(true);
                }
                else {
                    event.viewers().removeIf(audience ->
                            !(audience instanceof Player player && this.hasMember(player)));
                    ChatRenderer oldRenderer = event.renderer();
                    event.renderer((source, sourceDisplayName, message, viewer) -> {
                            Component render = oldRenderer.render(source, sourceDisplayName, message, viewer);
                            return MiniMessage.get().parse("<blue>Party <dark_gray>> <reset><message>",
                                    Template.of("message", render));
                    });
                }
            }
        }
    }

    /**
     * Registers a handler to be called when a player joins the party
     * @param joinHandler The handler to add
     */
    public void registerJoinHandler(@NotNull Consumer<PartyMember> joinHandler) {
        this.partyJoinHandlers.add(joinHandler);
    }

    /**
     * Registers a handler to be called when a player leaves the party
      * @param leaveHandler The handler to add
     */
    public void registerLeaveHandler(@NotNull Consumer<PartyMember> leaveHandler) {
        this.partyLeaveHandlers.add(leaveHandler);
    }

    /**
     * Adds a member to the party
     * @param player The new player
     * @return An optional of the party member that is present if the member is new
     */
    public @NotNull Optional<PartyMember> addMember(@NotNull Player player) {
        UUID memberUUID = player.getUniqueId();

        if (!this.members.containsKey(memberUUID)) {
            PartyMember partyMember = this.partyMemberBuilder.apply(player);
            this.members.put(memberUUID, partyMember);
            Integer taskId = this.invitationMap.remove(player.getUniqueId());
            if (taskId != null) {
                this.plugin.getServer().getScheduler().cancelTask(taskId);
            }

            this.broadcastMessage(MiniMessage.get().parse("<member> <yellow>has joined the party.",
                    Template.of("member", player.displayName())));

            for (Consumer<PartyMember> handler : partyJoinHandlers) {
                handler.accept(partyMember);
            }

            return Optional.of(partyMember);
        }

        return Optional.empty();
    }

    /**
     * Removes a member from the party
     * @param player The player to remove
     */
    public void removeMember(@NotNull OfflinePlayer player, boolean forced) {
        if (this.members.containsKey(player.getUniqueId())) {
            PartyMember removed = this.members.remove(player.getUniqueId());
            String message = (forced) ? "been removed from" : "left";

            Player onlinePlayer = player.getPlayer();

            Component name = (onlinePlayer != null)
                    ? onlinePlayer.displayName()
                    : MiniMessage.get().parse("<gray>" + player.getName());

            boolean clearHandlers = false;
            if (this.owner.equals(removed)) {
                chooseNewOwner();

                if (this.owner != null) {
                    OfflinePlayer offlineOwner = this.owner.getOfflinePlayer();
                    Player onlineOwner = offlineOwner.getPlayer();
                    Component toName = (onlineOwner != null)
                            ? onlineOwner.displayName()
                            : MiniMessage.get().parse("<gray>" + offlineOwner.getName());

                    this.broadcastMessage(MiniMessage.get().parse("<yellow>The party has been transferred " +
                            "to <to><reset><yellow>.", Template.of("to", toName)));
                }
                else {
                    cancelAllOutgoingInvitations();
                    clearHandlers = true;
                }
            }

            this.broadcastMessage(TextComponent.ofChildren(MiniMessage.get()
                    .parse(String.format("<member> <reset><yellow>has %s the party.", message),
                            Template.of("member", name))));

            Player removedPlayer = player.getPlayer();
            if (removedPlayer != null) {
                removedPlayer.sendMessage(MiniMessage.get()
                        .parse(String.format("<yellow>You have %s the party.", message)));
            }

            for (Consumer<PartyMember> handler : partyLeaveHandlers) {
                handler.accept(removed);
            }

            if (clearHandlers) {
                this.partyJoinHandlers.clear();
                this.partyLeaveHandlers.clear();
            }
        }
    }

    /**
     * Kicks all offline players
     * @return The kicked players
     */
    public @NotNull Collection<OfflinePlayer> kickOffline() {
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();

        boolean clearHandlers = false;
        Iterator<PartyMember> iterator = this.members.values().iterator();
        while (iterator.hasNext()) {
            PartyMember partyMember = iterator.next();
            OfflinePlayer player = partyMember.getOfflinePlayer();
            if (!player.isOnline()) {
                if (this.owner.equals(partyMember)) {
                    chooseNewOwner();

                    if (this.owner != null) {
                        OfflinePlayer offlineOwner = this.owner.getOfflinePlayer();
                        Player onlineOwner = offlineOwner.getPlayer();
                        Component name = (onlineOwner != null)
                                ? onlineOwner.displayName()
                                : Component.text(Objects.toString(offlineOwner.getName()), NamedTextColor.GRAY);

                        this.broadcastMessage(MiniMessage.get()
                                .parse("<gray>" + player.getName() + " <yellow>has been removed from " +
                                        "the party. The party has been transferred to <reset><owner><reset><yellow>.",
                                        Template.of("owner", name)));
                    }
                    else {
                        cancelAllOutgoingInvitations();
                        clearHandlers = true;
                    }
                }

                iterator.remove();
                offlinePlayers.add(player);

                for (Consumer<PartyMember> handler : partyLeaveHandlers) {
                    handler.accept(partyMember);
                }
            }
        }

        this.broadcastMessage(MiniMessage.get().parse(String.format("<red>%d offline players have been removed from " +
                "the party.", offlinePlayers.size())));

        if (clearHandlers) {
            this.partyJoinHandlers.clear();
            this.partyLeaveHandlers.clear();
        }

        return offlinePlayers;
    }

    private void cancelAllOutgoingInvitations() {
        Iterator<Integer> iterator = this.invitationMap.values().iterator();
        while (iterator.hasNext()) {
            Integer taskId = iterator.next();
            if (taskId != null) {
                this.plugin.getServer().getScheduler().cancelTask(taskId);
            }

            iterator.remove();
        }
    }

    /**
     * Toggles whether the party is muted
     */
    public void mute() {
        this.partySettings.setMuted(!this.partySettings.isMuted());
        this.broadcastMessage(MiniMessage.get().parse(String.format("<yellow>The party has been %s.",
                this.partySettings.isMuted() ? "muted" : "unmuted")));
    }

    /**
     * Toggles whether a player is mute in the party
     * @param player The player to toggle on
     */
    public void mutePlayer(@NotNull OfflinePlayer player) {
        PartyMember member = this.members.get(player.getUniqueId());
        if (member != null && member != this.owner) {
            member.setMuted(!member.isMuted());

            Component name = member.getPlayerIfOnline()
                    .map(Player::displayName)
                    .orElseGet(() -> Component.text(Objects.toString(player.getName()), NamedTextColor.GRAY));
            this.broadcastMessage(MiniMessage.get()
                    .parse("<member> <reset><yellow>has been %s.", Template.of("member", name)));
        }
    }

    private void chooseNewOwner() {
        List<PartyMember> offlineMembers = this.members.values().stream()
                .filter(member -> !this.owner.equals(member)).toList();
        List<PartyMember> onlineMembers = offlineMembers.stream()
                .filter(member -> member.getPlayerIfOnline().isPresent()).toList();

        if (onlineMembers.size() > 0) {
            this.owner = onlineMembers.get(RANDOM.nextInt(onlineMembers.size()));
        }
        else if (offlineMembers.size() > 0) {
            this.owner = offlineMembers.get(RANDOM.nextInt(offlineMembers.size()));
        }
        else {
            this.owner = null;
        }
    }

    /**
     * Disbands the party
     * @return The players that were in the party
     */
    public @NotNull Collection<OfflinePlayer> disband() {
        Collection<PartyMember> memberCollection = this.members.values();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        Component disband = MiniMessage.get().parse("<red>The party has been disbanded.");

        this.owner = null;

        Iterator<PartyMember> iterator = memberCollection.iterator();
        while (iterator.hasNext()) {
            PartyMember partyMember = iterator.next();
            OfflinePlayer offlinePlayer = partyMember.getOfflinePlayer();
            Player player = offlinePlayer.getPlayer();

            if (player != null) {
                player.sendMessage(disband);
            }

            iterator.remove();
            offlinePlayers.add(offlinePlayer);

            for (Consumer<PartyMember> handler : this.partyLeaveHandlers) {
                handler.accept(partyMember);
            }
        }
        cancelAllOutgoingInvitations();

        this.partyJoinHandlers.clear();
        this.partyLeaveHandlers.clear();

        return offlinePlayers;
    }

    /**
     * Invites a player to the party
     * @param invitee The person to be invited
     * @param inviter THe person that invited them
     */
    public void invitePlayer(@NotNull OfflinePlayer invitee, @NotNull OfflinePlayer inviter) {
        if (this.members.containsKey(inviter.getUniqueId())) {
            Optional<PartyMember> ownerOptional = this.getOwner();
            if (ownerOptional.isPresent()) {
                double expirationTime = this.partySettings.getInviteExpirationTime() / 20F;

                OfflinePlayer partyOwner = ownerOptional.get().getOfflinePlayer();
                Player onlinePartyOwner = partyOwner.getPlayer();
                Player onlineInviterPlayer = inviter.getPlayer();
                Player onlineInvitee = invitee.getPlayer();

                Component inviterComponent = (onlineInviterPlayer != null)
                        ? onlineInviterPlayer.displayName()
                        : Component.text(Objects.toString(inviter.getName()), NamedTextColor.GRAY);

                Component inviteeComponent = (onlineInvitee != null)
                        ? onlineInvitee.displayName()
                        : Component.text(Objects.toString(invitee.getName()), NamedTextColor.GRAY);

                String ownerName = Objects.toString(partyOwner.getName());

                Component ownerComponent = (onlinePartyOwner != null)
                        ? onlinePartyOwner.displayName()
                        : Component.text(ownerName, NamedTextColor.GRAY);

                if (onlineInvitee != null) {
                    Template ownerTemplate = Template.of("owner", ownerComponent);
                    onlineInvitee.sendMessage(MiniMessage.get().parse(String.format("<inviter> <reset><yellow>has " +
                                            "invited you to join <owner-msg> <reset><yellow>party! Click " +
                                            "<hover:show_text:'<yellow>/party join <reset><owner>'>" +
                                            "<click:run_command:/party join %s>" +
                                            "<red>here <yellow>to join! You have %.1f seconds to accept!",
                                    ownerName, expirationTime),
                            Template.of("inviter", inviterComponent),
                            (partyOwner.equals(inviter))
                                    ? Template.of("owner-msg", "their")
                                    : Template.of("owner-msg",
                                    MiniMessage.get().parse("<reset><owner>'s", ownerTemplate)), ownerTemplate));
                }

                this.broadcastMessage(MiniMessage.get().parse(String.format("<inviter> <reset><yellow>has invited " +
                                        "<reset><invitee> <reset><yellow>to the party! They have %.1f seconds " +
                                        "to accept.",
                                expirationTime),
                        Template.of("inviter", inviterComponent), Template.of("invitee", inviteeComponent)));

                this.invitationMap.put(invitee.getUniqueId(),
                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    this.invitationMap.remove(invitee.getUniqueId());

                    if (!this.hasMember(invitee)) {
                        this.broadcastMessage(MiniMessage.get().parse("<yellow>The invite to <reset><invitee> " +
                                "<reset><yellow>has expired.", Template.of("invitee", inviteeComponent)));

                        if (onlineInvitee != null && onlineInvitee.isOnline()) {
                            onlineInvitee.sendMessage(MiniMessage.get().parse("<yellow>The invite to " +
                                    "<reset><owner><reset><yellow>'s party has expired.",
                                    Template.of("owner", ownerComponent)));
                        }
                    }
                }, this.getPartySettings().getInviteExpirationTime()).getTaskId());
            }
        }
    }

    /**
     * Determines whether a player has an invite to this party
     * @param player The potentially invited player
     * @return Whether the player has an invite
     */
    public boolean hasInvite(@NotNull OfflinePlayer player) {
        return this.invitationMap.containsKey(player.getUniqueId());
    }

    /**
     * Gets a copy of the set of all outgoing invites
     * @return The set of invites
     */
    public @NotNull Set<UUID> getInvites() {
        return Set.copyOf(this.invitationMap.keySet());
    }

    /**
     * Determines if a player is the owner of the party
     * @param player The player to test
     * @return Whether the player is the party owner
     */
    public boolean isOwner(@NotNull OfflinePlayer player) {
        return this.owner.getOfflinePlayer().equals(player);
    }

    /**
     * Gets the owner of the party
     * @return The owner of the party
     */
    public @NotNull Optional<PartyMember> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    /**
     * Transfers the party to another player
     * @param player The player to transfer the party to
     */
    public void transferPartyToPlayer(@NotNull OfflinePlayer player) {
        PartyMember member = this.members.get(player.getUniqueId());

        if (member != null) {
            Component fromName = member.getPlayerIfOnline()
                    .map(Player::displayName)
                    .orElseGet(() -> Component.text(Objects.toString(player.getName()), NamedTextColor.GRAY));

            Player toPlayer = player.getPlayer();
            Component toName = (toPlayer != null)
                    ? toPlayer.displayName()
                    : Component.text(Objects.toString(player.getName()), NamedTextColor.YELLOW);

            this.broadcastMessage(MiniMessage.get().parse("<yellow>The party has been transferred " +
                    "from <reset><from> <reset><yellow>to <reset><to><reset><yellow>.",
                    Template.of("from", fromName), Template.of("to", toName)));

            this.owner = member;
        }
    }

    /**
     * Gets a party member
     * @param player The player
     * @return The party member corresponding to the player, or null if it does not exist
     */
    public @NotNull Optional<PartyMember> getMember(@NotNull OfflinePlayer player) {
        return Optional.ofNullable(this.members.get(player.getUniqueId()));
    }

    /**
     * Gets all party members
     * @return All of the party members
     */
    public @NotNull Collection<PartyMember> getMembers() {
        return new ArrayList<>(this.members.values());
    }

    /**
     * Gets all online players in the party
     * @return The online players in the party
     */
    public @NotNull List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();

        for (PartyMember partyMember : this.members.values()) {
            Optional<Player> partyMemberOptional = partyMember.getPlayerIfOnline();
            partyMemberOptional.ifPresent(players::add);
        }

        return players;
    }

    /**
     * Determines if the party has a member
     * @param player The member
     * @return Whether the party has the member
     */
    public boolean hasMember(@NotNull OfflinePlayer player) {
        return this.members.containsKey(player.getUniqueId());
    }

    /**
     * Broadcasts a message to the entire party
     * @param message The component to send
     */
    public void broadcastMessage(@NotNull Component message) {
        for (PartyMember member : this.members.values()) {
            member.getPlayerIfOnline().ifPresent(player -> player.sendMessage(message));
        }
    }

    /**
     * Gets a collection of components for a list of the members in the party
     * @return The collection of components
     */
    public @NotNull Collection<Component> getPartyListComponents() {
        TextComponent.Builder online = Component.text().append(MiniMessage.get().parse("<green>Online<white>: "));
        TextComponent.Builder offline = Component.text().append(MiniMessage.get().parse("<red>Offline<white>: "));
        TextComponent.Builder invited = Component.text()
                .append(MiniMessage.get().parse("<blue>Invites<white>: "));

        Collection<PartyMember> memberCollection = this.members.values();
        List<Player> onlinePlayers = new ArrayList<>(memberCollection.size());
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        for (PartyMember member : memberCollection) {
            OfflinePlayer offlinePlayer = member.getOfflinePlayer();
            Player onlinePlayer = offlinePlayer.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayers.add(onlinePlayer);
            } else {
                offlinePlayers.add(offlinePlayer);
            }
        }

        Component comma = MiniMessage.get().parse("<white>, ");
        for (int i = 0; i < onlinePlayers.size(); i++) {
            online.append(onlinePlayers.get(i).displayName());

            if (i < onlinePlayers.size() - 1) {
                online.append(comma);
            }
        }

        for (int i = 0; i < offlinePlayers.size(); i++) {
            offline.append(MiniMessage.get().parse("<red>" + offlinePlayers.get(i)));
            if (i < offlinePlayers.size() - 1) {
                offline.append(comma);
            }
        }

        Iterator<UUID> iterator = this.invitationMap.keySet().iterator();
        while (iterator.hasNext()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(iterator.next());
            Player onlinePlayer = offlinePlayer.getPlayer();

            invited.append((onlinePlayer != null)
                    ? onlinePlayer.displayName()
                    : MiniMessage.get().parse("<blue>" + offlinePlayer.getName()));

            if (iterator.hasNext()) {
                invited.append(comma);
            }
        }

        return List.of(online.build(), offline.build(), invited.build());
    }


    /**
     * Gets the unique identifier of this party
     * @return The UUID
     */
    public @NotNull UUID getId() {
        return this.uuid;
    }

    /**
     * Gets the party's settings
     * @return The settings
     */
    public @NotNull PartySettings getPartySettings() {
        return this.partySettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Party that = (Party) o;

        return Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

}
