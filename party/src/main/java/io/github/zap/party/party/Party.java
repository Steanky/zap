package io.github.zap.party.party;

import io.github.zap.party.PartyPlusPlus;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public Party(OfflinePlayer owner) {
        this.owner = new PartyMember(this, owner);
        this.partySettings = PartyPlusPlus.getInstance().getPartyManager().createPartySettings(owner);
    }

    public boolean addMember(@NotNull OfflinePlayer player) {
        String memberName = player.getName();

        if (memberName != null && !members.containsKey(memberName)) {
            members.put(memberName, new PartyMember(this, player));

            Component newPlayer = Component.text(memberName, NamedTextColor.GRAY)
                    .append(Component.text(" has joined the party.", NamedTextColor.YELLOW));

            for (PartyMember partyMember : getMembers()) {
                Player member = partyMember.getPlayer().getPlayer();
                if (member != null) {
                    member.sendMessage(newPlayer);
                }
            }

            return true;
        }

        return false;
    }

    public @Nullable PartyMember removeMember(@NotNull String name) {
        if (members.containsKey(name)) {
            PartyMember removed = members.remove(name);

            if (owner.equals(removed)) {
                PartyMember[] memberArray = members.values().toArray(ARRAY);

                owner = memberArray[RANDOM.nextInt(memberArray.length)];
                owner.setOwner(true);

                Component partyTransferred = Component.text(name, NamedTextColor.GRAY)
                        .append(Component.text(" has left the party. The party has been transferred to ",
                                NamedTextColor.YELLOW))
                        .append(Component.text(Objects.toString(owner.getPlayer().getName()), NamedTextColor.GRAY));

                for (PartyMember partyMember : getMembers()) {
                    Player member = partyMember.getPlayer().getPlayer();
                    if (member != null) {
                        member.sendMessage(partyTransferred);
                    }
                }

                return removed;
            }

            Component memberLeft = Component.text(name, NamedTextColor.GRAY)
                    .append(Component.text(" has left the party.", NamedTextColor.YELLOW));

            for (PartyMember partyMember : getMembers()) {
                Player member = partyMember.getPlayer().getPlayer();
                if (member != null) {
                    member.sendMessage(memberLeft);
                }
            }

            return removed;
        }

        return null;
    }

    public boolean hasMember(@NotNull String name) {
        return members.containsKey(name);
    }

    public Collection<PartyMember> getMembers() {
        return members.values();
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
