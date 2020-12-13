package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This abstract class contains some basic functionality all Arena objects share. This is not necessarily
 * Zombies-specific and could be used for other minigames, lobbies, etc.
 */
public abstract class Arena<T extends Arena<T>> implements Unique {
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
     * Attempts to add the players to arena. This should also teleport them to the arena's world.
     * @param joinAttempt The JoinInformation object to handle
     * @return If all of the players are added, this should return true. If not all are added for any reason, this
     * should return false.
     */
    public abstract boolean handleJoin(JoinInformation joinAttempt);

    /**
     * Removes the players from the arena. This method must always successfully remove the players. It should also
     * teleport them elsewhere.
     * @param leaveInformation The LeaveInformation object to handle
     */
    public abstract void handleLeave(LeaveInformation leaveInformation);

    /**
     * Terminates the arena, regardless of its state.
     */
    public abstract void terminate();
}