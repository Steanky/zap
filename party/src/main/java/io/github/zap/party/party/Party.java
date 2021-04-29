package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    @Getter
    private final Set<OfflinePlayer> invites = new HashSet<>();

    @Getter
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

            Component newPlayer = Component.text(memberName, NamedTextColor.GRAY)
                    .append(Component.text(" has joined the party.", NamedTextColor.YELLOW));

            broadcastMessage(newPlayer);
        }
    }

    /**
     * Removes a member from the party
     * @param name The name of the member to remove
     */
    public void removeMember(@NotNull String name) {
        if (members.containsKey(name)) {
            PartyMember removed = members.remove(name);

            if (owner.equals(removed)) {
                PartyMember[] memberArray = members.values().toArray(ARRAY);

                owner = memberArray[RANDOM.nextInt(memberArray.length)];

                Component partyTransferred = Component.text(name, NamedTextColor.GRAY)
                        .append(Component.text(" has left the party. The party has been transferred to ",
                                NamedTextColor.YELLOW))
                        .append(Component.text(Objects.toString(owner.getPlayer().getName()), NamedTextColor.GRAY));

                broadcastMessage(partyTransferred);
                return;
            }

            Component memberLeft = Component.text(name, NamedTextColor.GRAY)
                    .append(Component.text(" has left the party.", NamedTextColor.YELLOW));

            broadcastMessage(memberLeft);
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
