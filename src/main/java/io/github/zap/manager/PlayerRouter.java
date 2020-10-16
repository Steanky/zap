package io.github.zap.manager;

/**
 * This interface may or may not be necessary; i'm keeping it around for the time being though
 */
public interface PlayerRouter {
    /**
     * Routes the player(s) stored in JoinInformation.
     * @param information The JoinInformation
     * @return If the operation was successful (the player(s) joined the game), return true. Otherwise, return false
     */
    boolean route(JoinInformation information);
}
