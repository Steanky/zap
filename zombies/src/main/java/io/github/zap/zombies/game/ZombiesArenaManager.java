package io.github.zap.zombies.game;

import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.MapData;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
public class ZombiesArenaManager extends ArenaManager<ZombiesArena> {
    private static final String NAME = "zombies";
    private static final String DATA_KEY = "map";

    @Getter
    private final File dataFolder;

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    private final Map<String, MapData> maps = new HashMap<>();

    public ZombiesArenaManager(File dataFolder, int arenaCapacity, int arenaTimeout) {
        super(NAME);
        this.dataFolder = dataFolder;
        this.arenaCapacity = arenaCapacity;
        this.arenaTimeout = arenaTimeout;

        dataFolder.mkdirs();

        File[] files = dataFolder.listFiles();
        DataLoader loader = Zombies.getInstance().getDataLoader();
        MapData data = new MapData("test_world", "Test World", new BoundingBox(), new Vector(),
                1, 4, 10, 0, 10, false,
                false, true, true, 4, 20,
                20, Material.AIR);
        loader.save(data, Paths.get(dataFolder.getPath(), "test_map.yml").toFile(), DATA_KEY);

        if(files != null) {
            for(File file : files) {
                MapData map = loader.load(file, DATA_KEY);
                maps.put(map.getName(), map);
            }
        }
    }

    @Override
    public String getGameName() {
        return NAME;
    }

    public void handleJoin(JoinInformation information, Consumer<ImmutablePair<Boolean, String>> onCompletion) {
        for(Player player : information.getPlayers()) {
            if(!player.isOnline()) {
                onCompletion.accept(ImmutablePair.of(false, "RESOURCE_KEY_HERE"));
                return;
            }
        }

        String mapName = information.getMapName();
        long targetArena = information.getTargetArena();

        if(mapName != null) {
            for(ZombiesArena arena : arenas) {
                if(arena.getMap().getName().equals(mapName) && arena.handleJoin(information)) {
                    onCompletion.accept(ImmutablePair.of(true, null));
                    return;
                }
            }

            Zombies zombiesPlugin = Zombies.getInstance();
            Logger logger = zombiesPlugin.getLogger();
            if(managedArenas.size() < arenaCapacity) {
                logger.info(String.format("Loading arena for map '%s'", mapName));
                logger.fine(String.format("JoinInformation that triggered this load: '%s'", information));

                zombiesPlugin.getWorldLoader().loadWorld(mapName, (world) -> {
                    ZombiesArena arena = new ZombiesArena(this, world, maps.get(mapName), arenaTimeout);
                    managedArenas.put(arena.getId(), arena);

                    logger.info("Done loading arena.");

                    if(arena.handleJoin(information)) {
                        onCompletion.accept(ImmutablePair.of(true, null));
                    }
                    else {
                        Zombies.getInstance().getLogger().warning(String.format("Newly created arena rejected join " +
                                "request '%s'", information));
                        onCompletion.accept(ImmutablePair.of(false, "example.key"));
                    }
                });

                return;
            }
            else {
                logger.info("A JoinAttempt was rejected, as we have reached arena capacity.");
            }
        }
        else {
            ZombiesArena arena = managedArenas.get(targetArena);

            if(arena != null) {
                if(arena.handleJoin(information)) {
                    onCompletion.accept(ImmutablePair.of(true, null));
                }
                else {
                    onCompletion.accept(ImmutablePair.of(false, "example.key"));
                }
                return;
            }
            else {
                Zombies.getInstance().getLogger().warning(String.format("Requested arena '%s' does not exist.",
                        targetArena));
            }
        }

        onCompletion.accept(ImmutablePair.of(false, null));
    }

    @Override
    public boolean acceptsPlayers() {
        return false;
    }

    @Override
    public void closeArena(Arena<ZombiesArena> arena) {

    }

    @Override
    public void terminate() {
        for(ZombiesArena arena : arenas) {
            arena.terminate();
        }
    }
}