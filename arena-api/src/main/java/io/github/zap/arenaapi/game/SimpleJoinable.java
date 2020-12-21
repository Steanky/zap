package io.github.zap.arenaapi.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Used for testing purposes; actual Joinable implementations will probably be parties
 */
@RequiredArgsConstructor
public class SimpleJoinable implements Joinable {
    private final List<Player> players;

    @Override
    public boolean validate() {
        for(Player player : players) {
            if(!player.isOnline()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }
}
