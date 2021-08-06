package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A group of players which can join games together.
 */
public class Party {

    private final static Random RANDOM = new Random();

    private final static PartyMember[] ARRAY = new PartyMember[0];

    private final UUID uuid = UUID.randomUUID();

    private final PartySettings partySettings;

    private final Map<UUID, PartyMember> members = new HashMap<>();

    private final Set<OfflinePlayer> invites = new HashSet<>();

    private PartyMember owner;

    public Party(@NotNull PartyMember owner, @NotNull PartySettings partySettings) {
        this.owner = owner;
        this.partySettings = partySettings;

        members.put(owner.getOfflinePlayer().getUniqueId(), owner);
    }

    /**
     * Adds a member to the party
     * @param player The new player
     */
    public void addMember(@NotNull Player player) {
        UUID memberUUID = player.getUniqueId();

        if (!members.containsKey(memberUUID)) {
            members.put(memberUUID, new PartyMember(player));
            invites.remove(player);

            broadcastMessage(TextComponent.ofChildren(
                    player.displayName(),
                    Component.text(" has joined the party.", NamedTextColor.YELLOW)
            ));
        }
    }

    /**
     * Removes a member from the party
     * @param uuid The id of the member to remove
     */
    public void removeMember(@NotNull UUID uuid, boolean forced) {
        if (members.containsKey(uuid)) {
            PartyMember removed = members.remove(uuid);
            String message = (forced) ? "been removed from" : "left";

            OfflinePlayer offlinePlayer = owner.getOfflinePlayer();
            Player onlinePlayer = offlinePlayer.getPlayer();

            Component name = (onlinePlayer != null)
                    ? onlinePlayer.displayName()
                    : Component.text(Objects.toString(offlinePlayer.getName()), NamedTextColor.GRAY);

            if (owner.equals(removed)) {
                chooseNewOwner();

                if (owner != null) {
                    OfflinePlayer offlineOwner = owner.getOfflinePlayer();
                    Player onlineOwner = offlineOwner.getPlayer();
                    Component toName = (onlineOwner != null)
                            ? onlineOwner.displayName()
                            : Component.text(Objects.toString(offlineOwner.getName()), NamedTextColor.GRAY);

                    broadcastMessage(TextComponent.ofChildren(
                            name,
                            Component.text(" has been removed from the party. " +
                                    "The party has been transferred to ", NamedTextColor.YELLOW),
                            toName,
                            Component.text(".", NamedTextColor.YELLOW)
                    ));
                }

                // if there was nobody else in the party, there was nobody else to transfer the party to.
                return;
            }

            broadcastMessage(TextComponent.ofChildren(
                    name,
                    Component.text(String.format(" has %s the party.", message), NamedTextColor.YELLOW)
            ));

            removed.getPlayerIfOnline().ifPresent(oldOwner ->
                    oldOwner.sendMessage(Component.text(String.format("You have %s the party.", message),
                            NamedTextColor.YELLOW)));
        }
    }

    /**
     * Kicks all offline players
     * @return The kicked players
     */
    public Collection<OfflinePlayer> kickOffline() {
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();

        Iterator<PartyMember> iterator = members.values().iterator();
        while (iterator.hasNext()) {
            PartyMember partyMember = iterator.next();
            OfflinePlayer player = partyMember.getOfflinePlayer();
            if (!player.isOnline()) {
                if (owner.equals(partyMember)) {
                    chooseNewOwner();

                    if (owner != null) {
                        OfflinePlayer offlineOwner = owner.getOfflinePlayer();
                        Player onlineOwner = offlineOwner.getPlayer();
                        Component name = (onlineOwner != null)
                                ? onlineOwner.displayName()
                                : Component.text(Objects.toString(offlineOwner.getName()), NamedTextColor.GRAY);

                        broadcastMessage(TextComponent.ofChildren(
                                Component.text(Objects.toString(player.getName()), NamedTextColor.GRAY),
                                Component.text(" has been removed from the party. " +
                                        "The party has been transferred to ", NamedTextColor.YELLOW),
                                name,
                                Component.text(".", NamedTextColor.YELLOW)
                        ));
                    }
                }

                iterator.remove();
                offlinePlayers.add(player);
            }
        }

        Component kicked = Component.text(offlinePlayers.size(), NamedTextColor.RED)
                .append(Component.text(" offline players have been removed from the party.",
                        NamedTextColor.RED));
        broadcastMessage(kicked);

        return offlinePlayers;
    }

    /**
     * Toggles whether the party is muted
     */
    public void mute() {
        partySettings.setMuted(!partySettings.isMuted());
        Component muted = Component.text(String.format("The party has been %s.",
                partySettings.isMuted() ? "muted" : "unmuted"), NamedTextColor.YELLOW);

        broadcastMessage(muted);
    }

    /**
     * Toggles whether a player is mute in the party
     * @param player The player to toggle on
     */
    public void mutePlayer(@NotNull OfflinePlayer player) {
        PartyMember member = members.get(player.getUniqueId());
        if (member != null && member != owner) {
            member.setMuted(!member.isMuted());

            Component name = member.getPlayerIfOnline()
                    .map(Player::displayName)
                    .orElse(Component.text(Objects.toString(player.getName()), NamedTextColor.GRAY));
            broadcastMessage(TextComponent.ofChildren(
                    name,
                    Component.text(String.format(" has been %s.", member.isMuted() ? "muted" : "unmuted"),
                            NamedTextColor.YELLOW)
            ));
        }
    }

    private void chooseNewOwner() {
        List<PartyMember> memberArray = members.values().stream()
                .filter(member -> member.getOfflinePlayer().isOnline() && !owner.equals(member)).toList();

        if (memberArray.size() > 0) {
            owner = memberArray.get(RANDOM.nextInt(memberArray.size()));
        } else {
            PartyMember[] array = members.values().toArray(ARRAY);
            if (array.length > 0) {
                owner = array[0];
            }
        }
    }

    /**
     * Disbands the party
     * @return The players that were in the party
     */
    public @NotNull Collection<OfflinePlayer> disband() {
        Collection<PartyMember> memberCollection = members.values();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        Component disband = Component.text("The party has been disbanded.", NamedTextColor.RED);

        Iterator<PartyMember> iterator = memberCollection.iterator();
        while (iterator.hasNext()) {
            OfflinePlayer offlinePlayer = iterator.next().getOfflinePlayer();
            Player player = offlinePlayer.getPlayer();

            if (player != null) {
                player.sendMessage(disband);
            }

            iterator.remove();
            offlinePlayers.add(offlinePlayer);
        }

        return offlinePlayers;
    }

    /**
     * Adds a player invite
     * @param player The invited player
     */
    public void addInvite(@NotNull OfflinePlayer player) {
        invites.add(player);
    }

    /**
     * Removes a player invite
     * @param player The player invite to remove
     */
    public void removeInvite(@NotNull OfflinePlayer player) {
        invites.remove(player);
    }

    /**
     * Determines whether a player has an invite to this party
     * @param player The potentially invited player
     * @return Whether the player has an invite
     */
    public boolean hasInvite(@NotNull OfflinePlayer player) {
        return invites.contains(player);
    }

    /**
     * Determines if a player is the owner of the party
     * @param player The player to test
     * @return Whether the player is the party owner
     */
    public boolean isOwner(@NotNull OfflinePlayer player) {
        return owner.getOfflinePlayer().equals(player);
    }

    /**
     * Gets the owner of the party as an offline player
     * @return The owner of the party
     */
    public @NotNull OfflinePlayer getOwner() {
        return owner.getOfflinePlayer();
    }

    /**
     * Transfers the party to another player
     * @param player The player to transfer the party to
     */
    public void transferPartyToPlayer(@NotNull OfflinePlayer player) {
        PartyMember member = members.get(player.getUniqueId());

        if (member != null) {
            Component fromName = member.getPlayerIfOnline()
                    .map(Player::displayName)
                    .orElse(Component.text(Objects.toString(player.getName()), NamedTextColor.GRAY));

            Player toPlayer = player.getPlayer();
            Component toName = (toPlayer != null)
                    ? toPlayer.displayName()
                    : Component.text(Objects.toString(player.getName()), NamedTextColor.YELLOW);

            broadcastMessage(TextComponent.ofChildren(
                    Component.text("The party has been transferred from ", NamedTextColor.YELLOW),
                    fromName,
                    Component.text(" to ", NamedTextColor.YELLOW),
                    toName,
                    Component.text(".", NamedTextColor.YELLOW)
            ));

            owner = member;
        }
    }

    /**
     * Gets a party member
     * @param uuid The id of the player
     * @return The party member corresponding to the player, or null if it does not exist
     */
    public @NotNull Optional<PartyMember> getMember(@NotNull UUID uuid) {
        return Optional.ofNullable(members.get(uuid));
    }

    /**
     * Gets all online players in the party
     * @return The online players in the party
     */
    public @NotNull List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();

        for (PartyMember partyMember : members.values()) {
            Optional<Player> partyMemberOptional = partyMember.getPlayerIfOnline();
            partyMemberOptional.ifPresent(players::add);
        }

        return players;
    }

    /**
     * Determines if the party has a member
     * @param uuid The id of the member
     * @return Whether the party has the member
     */
    public boolean hasMember(@NotNull UUID uuid) {
        return members.containsKey(uuid);
    }

    /**
     * Broadcasts a message to the entire party
     * @param message The component to send
     */
    public void broadcastMessage(@NotNull Component message) {
        for (PartyMember member : members.values()) {
            member.getPlayerIfOnline().ifPresent(player -> player.sendMessage(message));
        }
    }

    /**
     * Gets a collection of components for a list of the members in the party
     * @return The collection of components
     */
    public Collection<Component> getPartyListComponents() {
        TextComponent.Builder online = Component.text()
                .append(Component.text("Online", NamedTextColor.GREEN))
                .append(Component.text(": ", NamedTextColor.WHITE));
        TextComponent.Builder offline = Component.text()
                .append(Component.text("Offline", NamedTextColor.RED))
                .append(Component.text(": ", NamedTextColor.WHITE));
        TextComponent.Builder invited = Component.text()
                .append(Component.text("Invites", NamedTextColor.BLUE))
                .append(Component.text(": ", NamedTextColor.WHITE));

        Collection<PartyMember> memberCollection = members.values();
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

        for (int i = 0; i < onlinePlayers.size(); i++) {
            online.append(onlinePlayers.get(i).displayName());

            if (i < onlinePlayers.size() - 1) {
                online.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }

        for (int i = 0; i < offlinePlayers.size(); i++) {
            offline.append(Component.text(Objects.toString(offlinePlayers.get(i)), NamedTextColor.RED));
            if (i < offlinePlayers.size() - 1) {
                offline.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }

        Iterator<OfflinePlayer> iterator = invites.iterator();
        while (iterator.hasNext()) {
            OfflinePlayer offlinePlayer = iterator.next();
            Player onlinePlayer = offlinePlayer.getPlayer();

            invited.append((onlinePlayer != null)
                    ? onlinePlayer.displayName()
                    : Component.text(Objects.toString(offlinePlayer.getName()), NamedTextColor.BLUE));

            if (iterator.hasNext()) {
                invited.append(Component.text(", ", NamedTextColor.WHITE));
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

        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}
