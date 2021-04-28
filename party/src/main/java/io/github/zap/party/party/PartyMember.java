package io.github.zap.party.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class PartyMember {

    private final Party party;

    private final OfflinePlayer player;

    @Setter
    private boolean isOwner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartyMember that = (PartyMember) o;

        return Objects.equals(party, that.party) && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(party, player);
    }

}
