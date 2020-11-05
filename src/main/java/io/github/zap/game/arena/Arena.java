package io.github.zap.game.arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class Arena {
    @Getter
    protected final World world;

    @Getter
    protected final List<Player> players = new ArrayList<>();

    @Getter
    protected final List<Player> spectators = new ArrayList<>();

    @Getter
    protected ArenaState state = ArenaState.PREGAME;

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
