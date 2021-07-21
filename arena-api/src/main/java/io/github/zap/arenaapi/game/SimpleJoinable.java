package io.github.zap.arenaapi.game;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Used for testing purposes; actual Joinable implementations will probably be parties
 */
public record SimpleJoinable(@NotNull List<Pair<List<Player>, Metadata>> groups) implements Joinable {

    @Override
    public boolean validate() {
        for (Pair<List<Player>, Metadata> group : groups) {
            for (Player player : group.getLeft()) {
                if (!player.isOnline()) {
                    return false;
                }
            }
        }

        return true;
    }

}
