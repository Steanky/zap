package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import io.github.zap.manager.JoinInformation;
import io.github.zap.manager.PlayerRouter;
import lombok.Getter;

public class ArenaManager implements PlayerRouter {
    @Getter
    private final int worldCapacity;

    /**
     * Creates a new ArenaManager with the specified capacity. Cannot be < 1
     * @param worldCapacity The capacity of the ArenaManager
     */
    public ArenaManager(int worldCapacity) {
        if(worldCapacity < 1) {
            throw new IllegalArgumentException("worldCapacity cannot be less than 1");
        }

        this.worldCapacity = worldCapacity;
    }

    @Override
    public boolean route(JoinInformation information) {
        /*
        This method would presumably search through its arenas, looking for one that has room for the player(s)
        if not, it would send to another server
         */
        return false;
    }
}
