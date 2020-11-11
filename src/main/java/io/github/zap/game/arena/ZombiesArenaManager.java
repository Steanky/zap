package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.game.data.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
@RequiredArgsConstructor
public class ZombiesArenaManager implements ArenaManager<ZombiesArena> {
    private static final String NAME = "zombies";

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    private final Map<String, MapData> maps = new HashMap<>();
    private final Map<String, ZombiesArena> arenaMappings = new HashMap<>();
    private final Collection<ZombiesArena> arenas = arenaMappings.values();

    @Override
    public String getName() {
        return NAME;
    }

    public void handleJoin(JoinInformation information, Consumer<ImmutablePair<Boolean, String>> onCompletion) {
        for(Player player : information.getPlayers()) {
            if(!player.isOnline()) {
                onCompletion.accept(ImmutablePair.of(false, ""));
                return;
            }
        }

        String mapName = information.getMapName();
        String targetArena = information.getTargetArena();

        if(mapName != null) {
            for(ZombiesArena arena : arenas) {
                if(arena.getMap().getName().equals(mapName) && arena.handleJoin(information)) {
                    onCompletion.accept(ImmutablePair.of(true, null));
                    return;
                }
            }

            ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
            Logger logger = zombiesPlugin.getLogger();
            if(arenaMappings.size() < arenaCapacity) {
                logger.info(String.format("Loading arena for map '%s'", mapName));
                logger.fine(String.format("JoinInformation that triggered this load: '%s'", information));

                zombiesPlugin.getWorldLoader().loadWorld(mapName, (world) -> {
                    ZombiesArena arena = new ZombiesArena(this, world, maps.get(mapName), arenaTimeout);
                    arenaMappings.put(arena.getName(), arena);

                    logger.info("Done loading arena.");

                    if(arena.handleJoin(information)) {
                        onCompletion.accept(ImmutablePair.of(true, null));
                    }
                    else {
                        ZombiesPlugin.getInstance().getLogger().warning(String.format("Newly created arena rejected" +
                                " join request '%s'", information));
                        arena.close();
                        onCompletion.accept(ImmutablePair.of(false,""));
                    }
                });

                return;
            }
            else {
                logger.info("A JoinAttempt was rejected, as we have reached arena capacity.");
            }
        }
        else if(targetArena != null) {
            ZombiesArena arena = arenaMappings.get(targetArena);

            if(arena != null) {
                if(arena.handleJoin(information)) {
                    onCompletion.accept(ImmutablePair.of(true, null));
                }
                else {
                    onCompletion.accept(ImmutablePair.of(false, ""));
                }
                return;
            }
            else {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("Requested arena '%s' does not exist.",
                        targetArena));
            }
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Invalid JoinInformation passed to Zombies" +
                    "ArenaManager (both mapName and targetArena are null): '%s'", information));
        }

        onCompletion.accept(ImmutablePair.of(false, null));
    }

    public void removeArena(String name) {
        arenaMappings.remove(name);
    }

    @Override
    public void shutdown() {
        for(ZombiesArena arena : arenas) {
            arena.close();
        }
    }
}