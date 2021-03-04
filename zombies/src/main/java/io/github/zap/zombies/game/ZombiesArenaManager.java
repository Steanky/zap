package io.github.zap.zombies.game;

import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.equipment.JacksonEquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.JacksonShopManager;
import io.github.zap.zombies.game.data.map.shop.ShopManager;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpManagerOptions;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpManager;
import io.github.zap.zombies.game.powerups.managers.PowerUpManager;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
public class ZombiesArenaManager extends ArenaManager<ZombiesArena> {
    private static final String NAME = "zombies";

    @Getter
    private final EquipmentManager equipmentManager;

    @Getter
    private final PowerUpManager powerUpManager;

    @Getter
    private final ShopManager shopManager;

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    @Getter
    private final DataLoader mapLoader;

    private final Map<String, MapData> maps = new HashMap<>();

    private final Set<String> markedForDeletion = new HashSet<>();

    public ZombiesArenaManager(Location hubLocation, DataLoader mapLoader, DataLoader equipmentLoader, DataLoader powerUpLoader, int arenaCapacity,
                               int arenaTimeout) {
        super(NAME, hubLocation);
        this.equipmentManager = new JacksonEquipmentManager(equipmentLoader);
        this.powerUpManager = new JacksonPowerUpManager(powerUpLoader, new JacksonPowerUpManagerOptions());
        ((JacksonPowerUpManager) this.powerUpManager).load();
        this.shopManager = new JacksonShopManager();
        this.arenaCapacity = arenaCapacity;
        this.arenaTimeout = arenaTimeout;
        this.mapLoader = mapLoader;

    }

    @Override
    public String getGameName() {
        return NAME;
    }

    public void handleJoin(JoinInformation information, Consumer<Pair<Boolean, String>> onCompletion) {
        if(!information.getJoinable().validate()) {
            onCompletion.accept(ImmutablePair.of(false, "Someone is offline and therefore unable to join!"));
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
                        getArenaCreated().callEvent(arena);
                        if(arena.handleJoin(information.getJoinable().getPlayers())) {
                            onCompletion.accept(ImmutablePair.of(true, null));
                        }
                        else {
                            Zombies.warning(String.format("Newly created arena rejected join request '%s'.", information));
                            onCompletion.accept(ImmutablePair.of(false, "Tried to make a new arena, but it couldn't accept all of the players!"));
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
                    onCompletion.accept(ImmutablePair.of(false, "The arena rejected the join request."));
                }

                return;
            }
            else {
                Zombies.warning(String.format("Specific requested arena '%s' does not exist.", targetArena));
            }
        }

        onCompletion.accept(ImmutablePair.of(false, "An unknown error occurred."));
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

    public MapData getMap(String name) {
        return maps.get(name);
    }

    public void addMap(MapData data) {
        String name = data.getName();

        if(maps.containsKey(name)) {
            throw new UnsupportedOperationException("cannot add a map that already exists");
        }

        maps.put(name, data);
    }

    public void removeMap(String name) {
        maps.remove(name);
    }

    public List<MapData> getMaps() {
        return new ArrayList<>(maps.values());
    }

    public void deleteOnDisable(String mapName) {
        if(maps.containsKey(mapName)) {
            markedForDeletion.add(mapName);
        }
    }

    public boolean canDelete(String mapName) {
        return markedForDeletion.contains(mapName);
    }

    public void loadMaps() throws LoadFailureException {
        Zombies.info("Loading maps. Changes will not apply to existing map data.");

        File[] files = mapLoader.getRootDirectory().listFiles();
        if(files != null) {
            Zombies.info(String.format("Found %s file(s) in the map directory.", files.length));

            for(File file : files) {
                MapData map = this.mapLoader.load(FilenameUtils.getBaseName(file.getName()), MapData.class);

                if(map != null) {
                    maps.put(map.getName(), map);
                    Zombies.info(String.format("Loaded MapData for '%s'", map.getName()));
                }
                else {
                    throw new LoadFailureException("Unable to properly load some of the provided map data.");
                }
            }
        }
    }
}
