package io.github.zap.game.arena;

import java.util.List;

public interface ArenaManager<T extends Arena> {
    /**
     * Handle the specified JoinInformation.
     * @param joinAttempt The JoinInformation object
     * @return This method should return false if the requested operation succeeded (ex. all of the users joined the
     * game)
     */
    boolean handleJoin(JoinInformation joinAttempt);
    void removeArena(String name);
    T getArena(String name);
    List<T> getArenas();
}
