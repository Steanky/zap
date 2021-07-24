package io.github.zap.zombies.game;

import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.arenaapi.stats.FileStatsManager;
import io.github.zap.arenaapi.stats.StatsCache;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentDataManager;
import io.github.zap.zombies.game.data.equipment.JacksonEquipmentDataManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.shop.JacksonShopDataManager;
import io.github.zap.zombies.game.data.shop.ShopDataManager;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpDataManager;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpManagerOptions;
import io.github.zap.zombies.game.powerups.managers.PowerUpDataManager;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.map.MapStats;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    private final EquipmentDataManager equipmentDataManager;

    @Getter
    private final PowerUpDataManager powerUpDataManager;

    @Getter
    private final ShopDataManager shopDataManager;

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    @Getter
    private final DataLoader mapLoader;

    private final Map<String, MapData> maps = new HashMap<>();

    private final Set<String> markedForDeletion = new HashSet<>();

    public ZombiesArenaManager(Location hubLocation, DataLoader mapLoader, DataLoader equipmentLoader,
                               DataLoader powerUpLoader, DataLoader playerStatsLoader, DataLoader mapStatsLoader,
                               int arenaCapacity, int arenaTimeout) {
        super(NAME, hubLocation, createStatsManager(playerStatsLoader, mapStatsLoader));
        this.equipmentDataManager = new JacksonEquipmentDataManager(equipmentLoader);
        this.powerUpDataManager = new JacksonPowerUpDataManager(powerUpLoader, new JacksonPowerUpManagerOptions());
        ((JacksonPowerUpDataManager) this.powerUpDataManager).load();
        this.shopDataManager = new JacksonShopDataManager();
        this.arenaCapacity = arenaCapacity;
        this.arenaTimeout = arenaTimeout;
        this.mapLoader = mapLoader;

    }

    private static StatsManager createStatsManager(DataLoader playerStatsLoader, DataLoader mapStatsLoader) {
        StatsManager statsManager = new FileStatsManager(Map.of(CacheInformation.PLAYER,
                playerStatsLoader, CacheInformation.MAP, mapStatsLoader));
        statsManager.registerCache(new StatsCache<>(CacheInformation.PLAYER, PlayerGeneralStats.class,
                CacheInformation.MAX_FREE_MAP_CACHE_SIZE));
        statsManager.registerCache(new StatsCache<>(CacheInformation.MAP, MapStats.class,
                CacheInformation.MAX_FREE_MAP_CACHE_SIZE));

        return statsManager;
    }

    @Override
    public void handleJoin(JoinInformation information, Consumer<Pair<Boolean, String>> onCompletion) {
        if (!information.joinable().validate()) {
            onCompletion.accept(Pair.of(false, "Someone is offline and therefore unable to join!"));
            return;
        }

        String mapName = information.mapName();
        UUID targetArena = information.targetArena();

        if (mapName != null) {
            MapData mapData = maps.get(mapName);

            if (mapData != null) {
                for (ZombiesArena arena : managedArenas.values()) {
                    if (arena.getMap().getName().equals(mapName) && arena.handleJoin(information.joinable())) {
                        onCompletion.accept(Pair.of(true, null));
                        return;
                    }
                }

                if (managedArenas.size() < arenaCapacity) {
                    Zombies.info(String.format("Loading arena for map '%s'.", mapName));
                    Zombies.info(String.format("JoinInformation that triggered this load: '%s'.", information));

                    Zombies.getInstance().getWorldLoader().loadWorld(mapData.getWorldName(), (world) -> {
                        world.setGameRule(GameRule.DO_FIRE_TICK, false);
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        world.setGameRule(GameRule.DO_INSOMNIA, false);
                        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
                        world.setGameRule(GameRule.MOB_GRIEFING, false);
                        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
                        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
                        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);

                        world.setTime(mapData.getWorldTime());

                        ZombiesArena arena = createArena();
                        managedArenas.put(arena.getId(), arena);
                        getArenaCreated().callEvent(arena);
                        if (arena.handleJoin(information.joinable())) {
                            onCompletion.accept(Pair.of(true, null));
                        }
                        else {
                            Zombies.warning(String.format("Newly created arena rejected join request '%s'.", information));
                            onCompletion.accept(Pair.of(false, "Tried to make a new arena, but it couldn't accept all of the players!"));
                        }
                    });

                } else {
                    onCompletion.accept(Pair.of(false, "The maximum arena capacity has been reached!"));
                }
            } else {
                onCompletion.accept(Pair.of(false, String.format("A map named '%s' does not exist.", mapName)));
            }
        } else if (targetArena != null) {
            ZombiesArena arena = managedArenas.get(targetArena);

            if (arena != null) {
                if (arena.handleJoin(information.joinable())) {
                    onCompletion.accept(Pair.of(true, null));
                } else {
                    onCompletion.accept(Pair.of(false, "The arena rejected the join request."));
                }
            } else {
                onCompletion.accept(Pair.of(false, String.format("Specific requested arena '%s' does not exist.",
                        targetArena)));
            }
        } else {
            onCompletion.accept(Pair.of(false, "A mapName or targetArena must be specified!"));
        }
    }

    @Override
    public boolean acceptsPlayers() {
        return true;
    }

    @Override
    public void unloadArena(ZombiesArena arena) {
        for(Player player : arena.getWorld().getPlayers()) { //tp out any players that could prevent us from unloading
            player.teleport(getHubLocation());
        }

        managedArenas.remove(arena.getId());

        //we are doing a single-world, single-arena approach so no need to check for other arenas sharing this world
        Zombies.getInstance().getWorldLoader().unloadWorld(arena.getWorld());
    }

    @Override
    public boolean hasMap(String mapName) {
        return maps.containsKey(mapName);
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

    protected @NotNull Arena<@NotNull ZombiesArena> createArena() {
        // TODO: implement
        return new ZombiesArena();
    }

}
