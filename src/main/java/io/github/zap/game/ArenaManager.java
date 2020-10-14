package io.github.zap.game;

import io.github.zap.manager.JoinInformation;
import io.github.zap.manager.PlayerRouter;

public class ArenaManager implements PlayerRouter {
    @Override
    public boolean route(JoinInformation information) {
        /*
        This method would presumably search through its arenas, looking for one that has room for the player(s)
         */
        return false;
    }
}
