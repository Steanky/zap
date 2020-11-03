package io.github.zap.game.manager;

import io.github.zap.ZombiesPlugin;
import io.github.zap.game.ZombiesArena;
import io.github.zap.game.JoinInformation;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
@RequiredArgsConstructor
public class ArenaManager {
    @Getter
    private final int arenaCapacity;

    @Getter
    private final Map<String, MapData> maps = new HashMap<>();

    @Getter
    private final Set<ZombiesArena> arenas = new HashSet<>();

    public boolean route(JoinInformation information) {
        for(ZombiesArena arena : arenas) {
            if(arena.handleJoin(information)) {
                return true;
            }
        }

        if(arenas.size() < arenaCapacity) {
            String mapName = information.getMapName();
            ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
            zombiesPlugin.getWorldLoader().loadWorld(mapName, (world) -> {
                ZombiesArena arena = new ZombiesArena(maps.get(mapName), world);
                zombiesPlugin.getTicker().register(arena);
                arenas.add(arena);
            });
        }

        return false;
    }
}
