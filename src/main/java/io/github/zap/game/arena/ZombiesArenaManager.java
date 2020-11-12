package io.github.zap.game.arena;

import io.github.zap.ZombiesPlugin;
import io.github.zap.game.data.MapData;
import io.github.zap.localization.MessageKey;
import io.github.zap.serialize.DataLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Paths;
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
    private static final String DATA_KEY = "map";

    @Getter
    private final File dataFolder;

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

    public void handleJoin(JoinInformation information, Consumer<ImmutablePair<Boolean, MessageKey>> onCompletion) {
        for(Player player : information.getPlayers()) {
            if(!player.isOnline()) {
                onCompletion.accept(ImmutablePair.of(false, MessageKey.EXAMPLE_KEY));
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
                        onCompletion.accept(ImmutablePair.of(false,MessageKey.EXAMPLE_KEY));
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
                    onCompletion.accept(ImmutablePair.of(false, MessageKey.LOCALE));
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

    @Override
    public void loadMaps() {
        dataFolder.mkdir();

        File[] files = dataFolder.listFiles();
        DataLoader loader = ZombiesPlugin.getInstance().getDataLoader();
        MapData data = new MapData("test_world", "Test World", new BoundingBox(), new Vector(),
                1, 4, 10, 0, 10, false,
                false, true, true, 4, 20,
                20, Material.AIR);
        loader.save(data, Paths.get(dataFolder.getName(), "test_map.yml").toFile(), DATA_KEY);

        if(files != null) {
            for(File file : files) {
                MapData map = loader.load(file, DATA_KEY);
                maps.put(map.getName(), map);
            }
        }
    }
}