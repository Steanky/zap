package io.github.zap.game.manager;

import lombok.Getter;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
public class ArenaManager {
    @Getter
    private final int arenaCapacity;

    /**
     * Creates a new ArenaManager with the specified capacity. Cannot be < 1.
     * @param arenaCapacity The capacity of the ArenaManager
     */
    public ArenaManager(int arenaCapacity) {
        if(arenaCapacity < 1) {
            throw new IllegalArgumentException("arenaCapacity cannot be less than 1");
        }

        this.arenaCapacity = arenaCapacity;
    }

    public boolean route(JoinInformation information) {
        /*
        This method would presumably search through its arenas, looking for one that has room for the player(s)
        if not, it would send to another server
         */
        return false;
    }
}
