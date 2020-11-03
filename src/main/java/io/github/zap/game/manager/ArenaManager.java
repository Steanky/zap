package io.github.zap.game.manager;

import io.github.zap.game.ZombiesArena;
import io.github.zap.game.JoinInformation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
@RequiredArgsConstructor
public class ArenaManager {
    @Getter
    private final int arenaCapacity;

    @Getter
    private final List<ZombiesArena> arenas = new ArrayList<>();

    public boolean route(JoinInformation information) {
        for(ZombiesArena arena : arenas) {
            if(arena.handleJoin(information)) {
                return true;
            }
        }

        return false;
    }
}
