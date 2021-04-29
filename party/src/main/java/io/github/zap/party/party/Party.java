package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A group of players which can join games together.
 */
public class Party {

    private final static Random RANDOM = new Random();

    private final static PartyMember[] ARRAY = new PartyMember[0];

    @Getter
    private final UUID uuid = UUID.randomUUID();

    @Getter
    private final PartySettings partySettings;

    private final Map<String, PartyMember> members = new HashMap<>();

    private final Set<OfflinePlayer> invites = new HashSet<>();

    private PartyMember owner;

    public Party(@NotNull OfflinePlayer owner) {
        this.owner = new PartyMember(this, owner);
        this.partySettings = PartyPlusPlus.getInstance().getPartyManager().createPartySettings(owner);

        members.put(owner.getName(), this.owner);
    }

    /**
     * Adds a member to the party
     * @param player The new player
     */
    public void addMember(@NotNull OfflinePlayer player) {
        String memberName = player.getName();

        if (memberName != null && !members.containsKey(memberName)) {
            members.put(memberName, new PartyMember(this, player));
            invites.remove(player);

            Component newPlayer = Component.text(memberName, NamedTextColor.GRAY)
                    .append(Component.text(" has joined the party.", NamedTextColor.YELLOW));

            broadcastMessage(newPlayer);
        }
    }

    /**
     * Removes a member from the party
     * @param name The name of the member to remove
     */
    public void removeMember(@NotNull String name, boolean forced) {
        if (members.containsKey(name)) {
            PartyMember removed = members.remove(name);
            String message = (forced) ? "been removed from" : "left";

            if (owner.equals(removed)) {
                chooseNewOwner();

                if (owner != null) {
                    Component partyTransferred = Component.text(name, NamedTextColor.GRAY)
                            .append(Component
                                    .text(String.format(" has %s the party. The party has been transferred to ",
                                            message), NamedTextColor.YELLOW))
                            .append(Component.text(Objects.toString(owner.getPlayer().getName()), NamedTextColor.GRAY));
                    broadcastMessage(partyTransferred);
                }
                // if there was nobody else in the party, there was nobody else to transfer the party to.

                return;
            }

            Component memberLeft = Component.text(name, NamedTextColor.GRAY)
                    .append(Component.text(String.format(" has %s the party.", message), NamedTextColor.YELLOW));
            broadcastMessage(memberLeft);

            Player player = removed.getPlayer().getPlayer();
            if (player != null) {
                player.sendMessage(Component.text(String.format("You have %s the party.", message),
                        NamedTextColor.YELLOW));
            }
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

            if (!partyMember.getPlayer().isOnline()) {
                if (owner.equals(partyMember)) {
                    chooseNewOwner();

                    if (owner != null) {
                        Component partyTransferred = Component.text(Objects.toString(partyMember.getPlayer().getName()),
                                NamedTextColor.GRAY)
                                .append(Component.text(" has been removed from the party. " +
                                                "The party has been transferred to ", NamedTextColor.YELLOW))
                                .append(Component.text(Objects.toString(owner.getPlayer().getName()),
                                        NamedTextColor.GRAY));
                        broadcastMessage(partyTransferred);
                    }
                    // if there was nobody else in the party, there was nobody else to transfer the party to.
                }

                iterator.remove();
                offlinePlayers.add(partyMember.getPlayer());
            }
        }

        Component kicked = Component.text(offlinePlayers.size(), NamedTextColor.RED)
                .append(Component.text(" offline players have been removed from the party.",
                        NamedTextColor.RED));
        broadcastMessage(kicked);

        return offlinePlayers;
    }

    private void chooseNewOwner() {
        List<PartyMember> memberArray = members.values().stream()
                .filter(member -> member.getPlayer().isOnline() && !owner.equals(member))
                .collect(Collectors.toUnmodifiableList());

        if (memberArray.size() > 0) {
            owner = memberArray.get(RANDOM.nextInt(memberArray.size()));
        } else {
            owner = members.values().toArray(ARRAY)[0];
        }
    }

    /**
     * Disbands the party
     * @return The players that were in the party
     */
    public Collection<OfflinePlayer> disband() {
        Collection<PartyMember> memberCollection = members.values();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        Component disband = Component.text("The party has been disbanded.", NamedTextColor.RED);

        Iterator<PartyMember> iterator = memberCollection.iterator();
        while (iterator.hasNext()) {
            OfflinePlayer offlinePlayer = iterator.next().getPlayer();
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
        return owner.getPlayer().equals(player);
    }

    /**
     * Gets the owner of the party as an offline player
     * @return The owner of the party
     */
    public @NotNull OfflinePlayer getOwner() {
        return owner.getPlayer();
    }

    /**
     * Transfers the party to another player
     * @param player The player to transfer the party to
     */
    public void transferPartyToPlayer(@NotNull OfflinePlayer player) {
        String playerName = player.getName();

        if (playerName != null) {
            PartyMember member = members.get(playerName);

            if (member != null) {
                Component transfer = Component.text("The party has been transferred from ",
                        NamedTextColor.YELLOW)
                        .append(Component.text(Objects.toString(owner.getPlayer().getName()), NamedTextColor.GRAY))
                        .append(Component.text(" to ", NamedTextColor.YELLOW))
                        .append(Component.text(playerName, NamedTextColor.GRAY))
                        .append(Component.text(".", NamedTextColor.YELLOW));

                broadcastMessage(transfer);

                owner = member;
            }
        }
    }

    /**
     * Determines if the party has a member
     * @param name The name of the member
     * @return Whether the party has the member
     */
    public boolean hasMember(@NotNull String name) {
        return members.containsKey(name);
    }

    /**
     * Broadcasts a message to the entire party
     * @param message The component to send
     */
    public void broadcastMessage(@NotNull Component message) {
        for (PartyMember partyMember : members.values()) {
            Player player = partyMember.getPlayer().getPlayer();

            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Gets a collection of components for a list of the members in the party
     * @return The collection of components
     */
    public Collection<Component> getPartyListComponents() {
        Component online = Component.text("Online", NamedTextColor.GREEN)
                .append(Component.text(": ", NamedTextColor.WHITE));
        Component offline = Component.text("Offline", NamedTextColor.RED)
                .append(Component.text(": ", NamedTextColor.WHITE));
        Component invited = Component.text("Invites", NamedTextColor.BLUE)
                .append(Component.text(": ", NamedTextColor.WHITE));

        Collection<PartyMember> memberCollection = members.values();
        List<OfflinePlayer> onlinePlayers = new ArrayList<>(memberCollection.size()),
                offlinePlayers = new ArrayList<>(memberCollection.size());

        for (PartyMember member : memberCollection) {
            OfflinePlayer player = member.getPlayer();
            String playerName = player.getName();

            if (playerName != null) {
                if (player.isOnline()) {
                    onlinePlayers.add(player);
                } else {
                    offlinePlayers.add(player);
                }
            }
        }

        for (int i = 0; i < onlinePlayers.size(); i++) {
            String playerName = onlinePlayers.get(i).getName();

            if (playerName != null) {
                online = online.append(Component.text(playerName, NamedTextColor.GREEN));

                if (i < onlinePlayers.size() - 1) {
                    online = online.append(Component.text(", ", NamedTextColor.WHITE));
                }
            }
        }

        for (int i = 0; i < offlinePlayers.size(); i++) {
            String playerName = offlinePlayers.get(i).getName();

            if (playerName != null) {
                online = online.append(Component.text(playerName, NamedTextColor.RED));

                if (i < offlinePlayers.size() - 1) {
                    online = online.append(Component.text(", ", NamedTextColor.WHITE));
                }
            }
        }

        Iterator<OfflinePlayer> iterator = invites.iterator();
        while (iterator.hasNext()) {
            OfflinePlayer next = iterator.next();
            String playerName = next.getName();

            if (playerName != null) {
                invited = invited.append(Component.text(playerName, NamedTextColor.BLUE));

                if (iterator.hasNext()) {
                    invited = invited.append(Component.text(", ", NamedTextColor.WHITE));
                }
            }
        }

        return List.of(online, offline, invited);
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
