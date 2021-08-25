package io.github.zap.party.party;

import io.github.zap.party.party.chat.PartyChatHandler;
import io.github.zap.party.party.invitation.InvitationManager;
import io.github.zap.party.party.list.PartyLister;
import io.github.zap.party.party.member.PartyMember;
import io.github.zap.party.party.member.PartyMemberBuilder;
import io.github.zap.party.party.namer.OfflinePlayerNamer;
import io.github.zap.party.party.settings.PartySettings;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A group of players which can join games together and chat together.
 */
public class Party {

    private final UUID uuid = UUID.randomUUID();

    private final Map<UUID, PartyMember> members = new HashMap<>();

    private final List<Consumer<PartyMember>> partyJoinHandlers = new ArrayList<>();

    private final List<Consumer<PartyMember>> partyLeaveHandlers = new ArrayList<>();

    private final MiniMessage miniMessage;

    private final Random random;

    private final PartySettings partySettings;

    private final PartyMemberBuilder partyMemberBuilder;

    private final InvitationManager invitationManager;

    private final PartyChatHandler partyChatHandler;

    private final PartyLister partyLister;

    private final OfflinePlayerNamer playerNamer;

    private PartyMember owner;

    /**
     * Creates a party.
     * @param miniMessage A {@link MiniMessage} instance to parse messages
     * @param random A {@link Random} instance used for random selections in parties
     * @param owner The owner of the party
     * @param partySettings The settings for the party
     * @param partyMemberBuilder A builder for new party members
     * @param invitationManager The invitation manager for this party
     * @param partyChatHandler A chat handler for chat events
     * @param partyLister A lister for party list components
     * @param playerNamer A namer for {@link Component} names of players
     */
    public Party(@NotNull MiniMessage miniMessage, @NotNull Random random, @NotNull PartyMember owner,
                 @NotNull PartySettings partySettings, @NotNull PartyMemberBuilder partyMemberBuilder,
                 @NotNull InvitationManager invitationManager, @NotNull PartyChatHandler partyChatHandler,
                 @NotNull PartyLister partyLister, @NotNull OfflinePlayerNamer playerNamer) {
        this.miniMessage = miniMessage;
        this.random = random;
        this.owner = owner;
        this.partySettings = partySettings;
        this.partyMemberBuilder = partyMemberBuilder;
        this.invitationManager = invitationManager;
        this.partyChatHandler = partyChatHandler;
        this.playerNamer = playerNamer;
        this.partyLister = partyLister;

        this.members.put(owner.getOfflinePlayer().getUniqueId(), owner);
    }

    /**
     * Called when this party should handle an {@link AsyncChatEvent}
     * @param event The event
     */
    public void onAsyncChat(AsyncChatEvent event) {
        this.partyChatHandler.onAsyncChat(this, event);
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

        if (this.members.containsKey(memberUUID)) {
            return Optional.empty();
        }

        PartyMember partyMember = this.partyMemberBuilder.createPartyMember(player);
        this.members.put(memberUUID, partyMember);
        this.invitationManager.removeInvitation(player);

        this.broadcastMessage(this.miniMessage.parse("<member> <yellow>has joined the party.",
                Template.of("member", player.displayName())));

        for (Consumer<PartyMember> handler : this.partyJoinHandlers) {
            handler.accept(partyMember);
        }

        return Optional.of(partyMember);
    }

    /**
     * Removes a member from the party
     * @param player The player to remove
     */
    public void removeMember(@NotNull OfflinePlayer player, boolean forced) {
        if (!this.members.containsKey(player.getUniqueId())) {
            return;
        }

        PartyMember removed = this.members.remove(player.getUniqueId());
        String message = (forced) ? "been removed from" : "left";

        Player onlinePlayer = player.getPlayer();

        Component name = (onlinePlayer != null)
                ? onlinePlayer.displayName()
                : this.miniMessage.parse("<gray>" + player.getName());

        boolean clearHandlers = false;
        if (this.owner.equals(removed)) {
            chooseNewOwner();

            if (this.owner != null) {
                OfflinePlayer offlineOwner = this.owner.getOfflinePlayer();
                Player onlineOwner = offlineOwner.getPlayer();
                Component toName = (onlineOwner != null)
                        ? onlineOwner.displayName()
                        : this.miniMessage.parse("<gray>" + offlineOwner.getName());

                this.broadcastMessage(this.miniMessage.parse("<yellow>The party has been transferred " +
                        "to <to><reset><yellow>.", Template.of("to", toName)));
            }
            else {
                this.invitationManager.cancelAllOutgoingInvitations();
                clearHandlers = true;
            }
        }

        this.broadcastMessage(TextComponent.ofChildren(this.miniMessage
                .parse("<member> <reset><yellow>has " + message + " the party.",
                        Template.of("member", name))));

        Player removedPlayer = player.getPlayer();
        if (removedPlayer != null && removedPlayer.isOnline()) {
            removedPlayer.sendMessage(this.miniMessage.parse("<yellow>You have " + message + " the party."));
        }

        for (Consumer<PartyMember> handler : this.partyLeaveHandlers) {
            handler.accept(removed);
        }

        if (clearHandlers) {
            this.partyJoinHandlers.clear();
            this.partyLeaveHandlers.clear();
        }
    }

    /**
     * Kicks all offline players.
     * @return The kicked players
     */
    @SuppressWarnings("UnusedReturnValue")
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
                        this.broadcastMessage(this.miniMessage.parse("<gray>" + player.getName() +
                                        " <yellow>has been removed from the party. The party has been transferred to " +
                                        "<reset><owner><reset><yellow>.",
                                Template.of("owner", this.playerNamer.name(this.owner.getOfflinePlayer()))));
                    }
                    else {
                        this.invitationManager.cancelAllOutgoingInvitations();
                        clearHandlers = true;
                    }
                }

                iterator.remove();
                offlinePlayers.add(player);

                for (Consumer<PartyMember> handler : this.partyLeaveHandlers) {
                    handler.accept(partyMember);
                }
            }
        }

        if (offlinePlayers.size() == 1) {
            this.broadcastMessage(this.miniMessage.parse("<red>1 offline player has been removed from " +
                    "the party."));
        }
        else {
            this.broadcastMessage(this.miniMessage.parse("<red>" + offlinePlayers.size() + " offline players " +
                    "have been removed from the party."));
        }

        if (clearHandlers) {
            this.partyJoinHandlers.clear();
            this.partyLeaveHandlers.clear();
        }

        return offlinePlayers;
    }

    /**
     * Toggles whether the party is muted
     */
    public void mute() {
        this.partySettings.setMuted(!this.partySettings.isMuted());
        this.broadcastMessage(this.miniMessage.parse("<yellow>The party has been "
                + (this.partySettings.isMuted() ? "muted" : "unmuted") + "."));
    }

    /**
     * Toggles whether a player is mute in the party
     * @param player The player to toggle on
     */
    public void mutePlayer(@NotNull OfflinePlayer player) {
        PartyMember member = this.members.get(player.getUniqueId());
        if (member != null && member != this.owner) {
            member.setMuted(!member.isMuted());
            this.broadcastMessage(this.miniMessage.parse("<member> <reset><yellow>has been "
                            + (member.isMuted() ? "muted" : "unmuted") + ".",
                    Template.of("member", this.playerNamer.name(member.getOfflinePlayer()))));
        }
    }

    private void chooseNewOwner() {
        if (this.members.size() == 0) {
            this.owner = null;
            return;
        }

        List<PartyMember> offlineMembers = new ArrayList<>(this.members.size() - 1);
        List<PartyMember> onlineMembers = new ArrayList<>(this.members.size() - 1);

        for (PartyMember partyMember : this.members.values()) {
            if (!this.owner.equals(partyMember)) {
                offlineMembers.add(partyMember);
            }
        }
        for (PartyMember partyMember : offlineMembers) {
            partyMember.getPlayerIfOnline().ifPresent(unused -> onlineMembers.add(partyMember));
        }

        if (onlineMembers.size() > 0) {
            this.owner = onlineMembers.get(this.random.nextInt(onlineMembers.size()));
        }
        else if (offlineMembers.size() > 0) {
            this.owner = offlineMembers.get(this.random.nextInt(offlineMembers.size()));
        }
        else {
            this.owner = null;
        }
    }

    /**
     * Disbands the party
     * @return The players that were in the party
     */
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Collection<OfflinePlayer> disband() {
        Collection<PartyMember> memberCollection = this.members.values();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        Component disband = this.miniMessage.parse("<red>The party has been disbanded.");

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
        this.invitationManager.cancelAllOutgoingInvitations();

        this.partyJoinHandlers.clear();
        this.partyLeaveHandlers.clear();

        return offlinePlayers;
    }

    /**
     * Gets the invitation manager for this party
     * @return The invitation manager
     */
    public @NotNull InvitationManager getInvitationManager() {
        return invitationManager;
    }

    /**
     * Determines if a player is the owner of the party
     * @param player The player to test
     * @return Whether the player is the party owner
     */
    public boolean isOwner(@NotNull OfflinePlayer player) {
        if (this.owner == null) {
            return false;
        }

        return this.owner.getOfflinePlayer().getUniqueId().equals(player.getUniqueId());
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

        if (member == null) {
            throw new IllegalArgumentException("Tried to transfer the party to a member that is not in the party!");
        }

        Component fromName = this.playerNamer.name(member.getOfflinePlayer());
        Component toName = this.playerNamer.name(player);

        this.broadcastMessage(this.miniMessage.parse("<yellow>The party has been transferred " +
                "from <reset><from> <reset><yellow>to <reset><to><reset><yellow>.",
                Template.of("from", fromName), Template.of("to", toName)));

        this.owner = member;
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
     * Gets the {@link PartyLister} for party list components
     * @return The {@link PartyLister}
     */
    public @NotNull PartyLister getPartyLister() {
        return this.partyLister;
    }

    /**
     * Gets the unique identifier of this party
     * @return The {@link UUID}
     */
    @SuppressWarnings("unused")
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        Party that = (Party) o;
        return Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

}
