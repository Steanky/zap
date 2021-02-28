package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * This abstract class contains some basic functionality all Arena objects share. This is not necessarily
 * Zombies-specific and could be used for other minigames, lobbies, etc.
 */
public abstract class Arena<T extends Arena<T>> implements Unique, Disposable {
    @Getter
    protected final ArenaManager<T> manager;

    @Getter
    protected final World world;

    @Getter
    protected final UUID id;

    public Arena(ArenaManager<T> manager, World world) {
        this.manager = manager;
        this.world = world;
        id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Arena) {
            return id == ((Arena<?>)other).id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Attempts to add the players to arena. This should also teleport them to the arena's world. This list of players
     * may include some players who are already in the arena in addition to players who are elsewhere, including other
     * arenas. It may not include offline players; these must be filtered out by the ArenaManager.
     * @param joining The players to attempt to add
     * @return If all of the players are added, this should return true. If not all are added for any reason, this
     * should return false.
     */
    public abstract boolean handleJoin(List<Player> joining);

    /**
     * Removes the players from the arena. This method must always successfully remove any players present in the arena;
     * ignoring ones that aren't.
     * @param leaving The leaving players
     */
    public abstract void handleLeave(List<Player> leaving);
}
