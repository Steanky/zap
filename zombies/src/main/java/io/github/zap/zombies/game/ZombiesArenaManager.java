package io.github.zap.zombies.game;

import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Location;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
public class ZombiesArenaManager extends ArenaManager<ZombiesArena> {
    private static final String NAME = "zombies";

    @Getter
    private final File dataFolder;

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    @Getter
    private final Map<String, MapData> maps = new HashMap<>();

    public ZombiesArenaManager(Location hubLocation, File dataFolder, int arenaCapacity, int arenaTimeout) throws LoadFailureException {
        super(NAME, hubLocation);
        this.dataFolder = dataFolder;
        this.arenaCapacity = arenaCapacity;
        this.arenaTimeout = arenaTimeout;

        //noinspection ResultOfMethodCallIgnored
        dataFolder.mkdirs();

        File[] files = dataFolder.listFiles();
        DataLoader loader = Zombies.getInstance().getDataLoader();

        if(files != null) {
            for(File file : files) {
                MapData map = loader.load(file, MapData.class);

                if(map != null) {
                    maps.put(map.getName(), map);
                }
                else {
                    throw new LoadFailureException("Unable to properly load some of the provided map data.");
                }
            }
        }
    }

    @Override
    public String getGameName() {
        return NAME;
    }

    public void handleJoin(JoinInformation information, Consumer<ImmutablePair<Boolean, String>> onCompletion) {
        if(!information.getJoinable().validate()) {
            onCompletion.accept(ImmutablePair.of(false, MessageKey.OFFLINE_ARENA_REJECTION.getKey()));
            return;
        }

        String mapName = information.getMapName();
        UUID targetArena = information.getTargetArena();

        if(mapName != null) {
            MapData mapData = maps.get(mapName);

            if(mapData != null) {
                for(ZombiesArena arena : arenas) {
                    if(arena.getMap().getName().equals(mapName) && arena.handleJoin(information.getJoinable().getPlayers())) {
                        onCompletion.accept(ImmutablePair.of(true, null));
                        return;
                    }
                }

                if(managedArenas.size() < arenaCapacity) {
                    Zombies.info(String.format("Loading arena for map '%s'.", mapName));
                    Zombies.info(String.format("JoinInformation that triggered this load: '%s'.", information));

                    Zombies.getInstance().getWorldLoader().loadWorld(mapData.getWorldName(), (world) -> {
                        ZombiesArena arena = new ZombiesArena(this, world, maps.get(mapName), arenaTimeout);
                        managedArenas.put(arena.getId(), arena);

                        if(arena.handleJoin(information.getJoinable().getPlayers())) {
                            onCompletion.accept(ImmutablePair.of(true, null));
                        }
                        else {
                            Zombies.warning(String.format("Newly created arena rejected join request '%s'.", information));
                            onCompletion.accept(ImmutablePair.of(false, MessageKey.NEW_ARENA_REJECTION.getKey()));
                        }
                    });

                    return;
                }
                else {
                    Zombies.info("A JoinAttempt was rejected, as we have reached arena capacity.");
                }
            }
            else {
                Zombies.warning(String.format("A map named '%s' does not exist.", mapName));
            }
        }
        else {
            ZombiesArena arena = managedArenas.get(targetArena);

            if(arena != null) {
                if(arena.handleJoin(information.getJoinable().getPlayers())) {
                    onCompletion.accept(ImmutablePair.of(true, null));
                }
                else {
                    onCompletion.accept(ImmutablePair.of(false, MessageKey.GENERIC_ARENA_REJECTION.getKey()));
                }

                return;
            }
            else {
                Zombies.warning(String.format("Specific requested arena '%s' does not exist.", targetArena));
            }
        }

        onCompletion.accept(ImmutablePair.of(false, MessageKey.UNKNOWN_ARENA_REJECTION.getKey()));
    }

    @Override
    public boolean acceptsPlayers() {
        return true;
    }

    @Override
    public void removeArena(ZombiesArena arena) {
        managedArenas.remove(arena.getId());

        //we are doing a single-world, single-arena approach so no need to check for other arenas sharing this world
        Zombies.getInstance().getWorldLoader().unloadWorld(arena.getWorld());
    }

    @Override
    public boolean hasMap(String mapName) {
        return maps.containsKey(mapName);
    }

    @Override
    public void dispose() {
        for(ZombiesArena arena : arenas) {
            arena.dispose();
        }
    }
}
