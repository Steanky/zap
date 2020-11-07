package io.github.zap.game.arena;

import io.github.zap.game.Unique;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

/**
 * This abstract class contains some basic functionality all Arena objects share. This is not necessarily
 * Zombies-specific and could be used for other minigames, lobbies, etc.
 */
@RequiredArgsConstructor
public abstract class Arena implements Unique {
    @Getter
    protected final World world;

    @Override
    public boolean equals(Object other) {
        if(other instanceof Arena) {
            return world.getName().equals(((Arena)other).world.getName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return world.getName().hashCode();
    }

    @Override
    public String getName() {
        return world.getName();
    }

    /**
     * Attempts to add the players to arena.
     * @param joinAttempt The JoinInformation object to handle
     * @return If all of the players are added, this should return true. If not all are added for any reason, this
     * should return false.
     */
    public abstract boolean handleJoin(JoinInformation joinAttempt);

    /**
     * Removes the players from the arena. This method must always successfully remove the players.
     * @param leaveInformation The LeaveInformation object to handle
     */
    public abstract void handleLeave(LeaveInformation leaveInformation);

    /**
     * Shuts down the arena. This method should perform any necessary cleanup, such as removing listeners and unloading
     * worlds.
     */
    public abstract void close();
}
