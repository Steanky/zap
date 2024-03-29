package io.github.zap.zombies;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.*;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.stream.NBTOutputStream;
import com.grinderwolf.swm.plugin.loaders.file.FileLoader;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.playerdata.FilePlayerDataManager;
import io.github.zap.arenaapi.playerdata.PlayerDataManager;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.JacksonDataLoader;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.time.StopWatch;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.command.ZombiesCommand;
import io.github.zap.zombies.command.mapeditor.ContextManager;
import io.github.zap.zombies.command.mapeditor.MapeditorCommand;
import io.github.zap.zombies.game.ZombiesArenaManager;
import io.github.zap.zombies.game.mob.goal.mythicmobs.WrappedMythicBreakWindow;
import io.github.zap.zombies.game.mob.goal.mythicmobs.WrappedMythicOptimizedBowAttack;
import io.github.zap.zombies.game.mob.goal.mythicmobs.WrappedMythicOptimizedMeleeAttack;
import io.github.zap.zombies.game.mob.mechanic.*;
import io.github.zap.zombies.game.npc.ZombiesNPC;
import io.github.zap.zombies.nms.common.NMSBridge;
import io.github.zap.zombies.nms.v1_16_R3.NMSBridge_v1_16_R3;
import io.github.zap.zombies.world.SlimeWorldLoader;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfinderAdapter;
import io.lumine.xikage.mythicmobs.skills.SkillManager;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import io.lumine.xikage.mythicmobs.volatilecode.handlers.VolatileAIHandler;
import io.lumine.xikage.mythicmobs.volatilecode.v1_16_R3.VolatileAIHandler_v1_16_R3;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public final class Zombies extends JavaPlugin implements Listener {
    @Getter
    private static Zombies instance; //singleton for our main plugin class

    @Getter
    private NMSBridge nmsBridge;

    @Getter
    private ArenaApi arenaApi;

    @Getter
    private SlimePlugin SWM;

    @Getter
    private File slimeWorldDirectory;

    @Getter
    private String slimeExtension;

    @Getter
    private SlimeLoader slimeLoader;

    @Getter
    private MythicMobs mythicMobs;

    @Getter
    private WorldLoader worldLoader;

    @Getter
    private ZombiesArenaManager arenaManager;

    @Getter
    private ContextManager contextManager;

    @Getter
    private PlayerDataManager playerDataManager;

    @Getter
    private LocalizationManager localizationManager;

    @Getter
    private CommandManager commandManager;

    private final List<ZombiesNPC> zombiesNPCS = new ArrayList<>();

    public static final String DEFAULT_LOCALE = "en_US";
    public static final String LOCALIZATION_FOLDER_NAME = "localization";
    public static final String MAP_FOLDER_NAME = "maps";
    public static final String EQUIPMENT_FOLDER_NAME = "equipments";
    public static final String PLAYER_STATS_FOLDER_NAME = "stats/player";
    public static final String MAP_STATS_FOLDER_NAME = "stats/map";
    public static final String POWERUPS_FOLDER_NAME = "powerups";
    public static final String PLAYER_DATA_FOLDER_NAME = "playerdata";

    public static final String ARENA_METADATA_NAME = "zombies_arena";
    public static final String SPAWNINFO_WAVE_METADATA_NAME = "spawninfo_wave_metadata";
    public static final String WINDOW_METADATA_NAME = "spawn_window";

    @Override
    public void onEnable() {
        java.util.Collections.singletonList(null); //this is needed somehow

        StopWatch timer = new StopWatch();
        timer.start();
        instance = this;

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            initBridge();
            initDependencies();
            initConfig();
            initPathfinding(WrappedMythicBreakWindow.class, WrappedMythicOptimizedMeleeAttack.class,
                    WrappedMythicOptimizedBowAttack.class);
            initMechanics(CobwebMechanic.class, SpawnMobMechanic.class, StealCoinsMechanic.class,
                    SlowFireRateMechanic.class, SummonMountMechanic.class, TeleportBehindTargetMechanic.class);
            initPlayerDataManager();
            initLocalization();
            initWorldLoader();
            initArenaManagers();
            initNPCs();
            initCommands();
        }
        catch(LoadFailureException exception)
        {
            severe(String.format("A fatal error occurred that prevented the plugin from enabling properly: '%s'.",
                    exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
            // getServer().shutdown();
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        timer.stop();
        info(String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    @Override
    public void onDisable() {
        if(playerDataManager != null) {
            playerDataManager.flushAll(); //ensures any unsaved playerdata is saved when the plugin shuts down
        }

        if(arenaManager != null) {
            DataLoader loader = arenaManager.getMapLoader(); //save map data in case it was edited

            for(File file : loader.getRootDirectory().listFiles()) { //delete map data that shouldn't exist
                String fileNameWithExtension = file.getName();

                if(fileNameWithExtension.endsWith(arenaManager.getMapLoader().getExtension())) {
                    String filename = FilenameUtils.getBaseName(fileNameWithExtension);

                    if(arenaManager.canDelete(filename)) {
                        try {
                            Files.delete(file.toPath());
                            Zombies.info(String.format("Deleted marked map file: '%s'", filename));
                        } catch (IOException e) {
                            warning(String.format("Failed to delete map file %s: %s", fileNameWithExtension, e.getMessage()));
                        }
                    }
                }
            }
        }

        for (ZombiesNPC zombiesNPC : zombiesNPCS) {
            zombiesNPC.destroy();
        }
    }

    private void initBridge() throws LoadFailureException {
        nmsBridge = NMSBridge.selectBridge(NMSBridge_v1_16_R3.INSTANCE);

        if(nmsBridge == null) {
            throw new LoadFailureException(String.format("Unsupported NMS package version '%s'.", io.github.zap.arenaapi.nms.common.NMSBridge.CURRENT_NMS_VERSION));
        }
    }

    private void initConfig() {
        ConfigurationSerialization.registerClass(ZombiesNPC.ZombiesNPCData.class);

        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.addDefault(ConfigNames.DATA_CACHE_CAPACITY, 2048);
        config.addDefault(ConfigNames.DEFAULT_LOCALE, DEFAULT_LOCALE);
        config.addDefault(ConfigNames.LOCALIZATION_DIRECTORY, Path.of(getDataFolder().getPath(),
                LOCALIZATION_FOLDER_NAME).toFile().getPath());
        config.addDefault(ConfigNames.WORLD_SPAWN, new Vector(0, 1, 0));
        config.addDefault(ConfigNames.LOBBY_WORLD, ArenaApi.getInstance().getNmsBridge().worldBridge()
                .getDefaultWorldName());
        config.addDefault(ConfigNames.NPC_LIST, Collections.emptyList());

        config.options().copyDefaults(true);
        saveConfig();
    }

    private void initDependencies() throws LoadFailureException {
        arenaApi = ArenaApi.getDependentPlugin(PluginNames.ARENA_API, true, true);
        SWM = ArenaApi.getDependentPlugin(PluginNames.SLIME_WORLD_MANAGER, true, true);
        mythicMobs = ArenaApi.getDependentPlugin(PluginNames.MYTHIC_MOBS, true,false);
        fixAswm();
    }

    private void fixAswm() {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.grinderwolf.swm.internal.com.flowpowered.nbt.stream.NBTOutputStream$1");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        clazz = NBTOutputStream.class;
        clazz = Tag.class;
        clazz = TagType.class;
        clazz = EndTag.class;
        clazz = ByteTag.class;
        clazz = ShortTag.class;
        clazz = IntTag.class;
        clazz = LongTag.class;
        clazz = FloatTag.class;
        clazz = DoubleTag.class;
        clazz = ByteArrayTag.class;
        clazz = StringTag.class;
        clazz = ListTag.class;
        clazz = CompoundTag.class;
        clazz = IntArrayTag.class;
        clazz = LongArrayTag.class;
        clazz = ShortArrayTag.class;
    }

    @SafeVarargs
    private void initPathfinding(Class<? extends PathfinderAdapter>... customGoals) throws LoadFailureException {
        VolatileAIHandler handler = mythicMobs.getVolatileCodeHandler().getAIHandler();

        if(handler instanceof VolatileAIHandler_v1_16_R3 target) {
            try {
                Field aiGoalsField = VolatileAIHandler_v1_16_R3.class.getDeclaredField("AI_GOALS");
                aiGoalsField.setAccessible(true);

                @SuppressWarnings("unchecked") Map<String, Class<? extends PathfinderAdapter>> aiGoals =
                        (Map<String, Class<? extends PathfinderAdapter>>)aiGoalsField.get(target);

                for(Class<? extends PathfinderAdapter> customGoal : customGoals) {
                    MythicAIGoal mythicAnnotation = customGoal.getAnnotation(MythicAIGoal.class);

                    if(mythicAnnotation != null) {
                        aiGoals.put(mythicAnnotation.name().toUpperCase(), customGoal);

                        for(String alias : mythicAnnotation.aliases()) {
                            aiGoals.put(alias.toUpperCase(), customGoal);
                        }

                        info("Loaded custom AI goal " + customGoal.getName());
                    }
                    else {
                        warning("Class " + customGoal.getName() + " should be annotated with @MythicAIGoal!");
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                throw new LoadFailureException("Reflection-related exception when initializing pathfinding.");
            }
        }
        else {
            throw new LoadFailureException("Unsupported version of MythicMobs AIHandler!");
        }
    }

    @SafeVarargs
    private void initMechanics(Class<? extends SkillMechanic>... customMechanics) throws LoadFailureException {
        try {
            Field mechanicsField = SkillManager.class.getDeclaredField("MECHANICS");
            mechanicsField.setAccessible(true);

            @SuppressWarnings("unchecked") Map<String, Class<? extends SkillMechanic>> mechanics =
                    (Map<String, Class<? extends SkillMechanic>>)mechanicsField.get(null);

            for(Class<? extends SkillMechanic> customMechanic : customMechanics) {
                MythicMechanic mythicAnnotation = customMechanic.getAnnotation(MythicMechanic.class);

                if(mythicAnnotation != null) {
                    mechanics.put(mythicAnnotation.name().toUpperCase(), customMechanic);

                    for(String alias : mythicAnnotation.aliases()) {
                        mechanics.put(alias.toUpperCase(), customMechanic);
                    }

                    info("Loaded custom MythicMobs mechanic " + customMechanic.getName());
                }
                else {
                    throw new LoadFailureException("Class " + customMechanic.getName() + " should be annotated with @MythicMechanic!");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new LoadFailureException("Reflection-related exception when initializing mechanics.");
        }
    }

    private void initWorldLoader() {
        info("Preloading worlds.");

        StopWatch timer = new StopWatch();
        timer.start();
        slimeWorldDirectory = new File("slime_worlds");
        slimeExtension = ".slime";
        slimeLoader = new FileLoader(slimeWorldDirectory); // this is the only instance of swm-plugin code, wish we could remove it
        worldLoader = new SlimeWorldLoader(slimeLoader);
        worldLoader.preload();
        timer.stop();

        info(String.format("Done preloading worlds; ~%sms elapsed.", timer.getTime()));
    }

    private void initArenaManagers() throws LoadFailureException {
        FileConfiguration config = getConfig();
        Vector spawn = config.getVector(ConfigNames.WORLD_SPAWN);
        String worldName = config.getString(ConfigNames.LOBBY_WORLD);

        if(spawn != null && worldName != null) {
            World world = Bukkit.getWorld(worldName);

            if(world != null) {
                DataLoader equipmentLoader = new JacksonDataLoader(new File(getDataFolder().getPath(),
                        EQUIPMENT_FOLDER_NAME));

                DataLoader powerupLoader = new JacksonDataLoader(new File(getDataFolder().getPath(),
                        POWERUPS_FOLDER_NAME));

                DataLoader mapLoader = new JacksonDataLoader(new File(getDataFolder().getPath(), MAP_FOLDER_NAME));

                DataLoader playerStatsLoader = new JacksonDataLoader(new File(getDataFolder().getPath(),
                        PLAYER_STATS_FOLDER_NAME));
                DataLoader mapStatsLoader = new JacksonDataLoader(new File(getDataFolder().getPath(),
                        MAP_STATS_FOLDER_NAME));

                arenaManager = new ZombiesArenaManager(WorldUtils.locationFrom(world, spawn), mapLoader,
                        equipmentLoader, powerupLoader, playerStatsLoader, mapStatsLoader,
                        config.getInt(ConfigNames.MAX_WORLDS), config.getInt(ConfigNames.ARENA_TIMEOUT));
                arenaManager.loadMaps();
                arenaApi.registerArenaManager(arenaManager);
            }
            else {
                throw new LoadFailureException(String.format("Specified lobby world '%s' does not exist.", worldName));
            }
        }
        else {
            throw new LoadFailureException("Unable to load required configuration information for ZombiesArenaManager.");
        }
    }

    private void initPlayerDataManager() {
        playerDataManager = new FilePlayerDataManager(new JacksonDataLoader(Path.of(getDataFolder().getPath(),
                PLAYER_DATA_FOLDER_NAME).toFile()), getConfig().getInt(ConfigNames.DATA_CACHE_CAPACITY));
    }

    private void initLocalization() throws LoadFailureException {
        Configuration config = getConfig();

        String locale = config.getString(ConfigNames.DEFAULT_LOCALE);
        String localizationDirectory = config.getString(ConfigNames.LOCALIZATION_DIRECTORY);

        if(locale != null && localizationDirectory != null) {
            localizationManager = new LocalizationManager(Locale.forLanguageTag(locale),
                    new File(localizationDirectory), playerDataManager);
        }
        else {
            throw new LoadFailureException("One or more required configuration entries could not be retrieved.");
        }
    }

    private void initCommands() {
        commandManager = new CommandManager(this);
        commandManager.registerCommand(new ZombiesCommand());
        commandManager.registerCommand(new MapeditorCommand());

        contextManager = new ContextManager();
    }

    private void initNPCs() {
        FileConfiguration config = getConfig();

        //noinspection unchecked
        List<ZombiesNPC.ZombiesNPCData> zombiesNPCDataList
                = (List<ZombiesNPC.ZombiesNPCData>) config.getList(ConfigNames.NPC_LIST);

        if (zombiesNPCDataList != null) {
            String lobbyWorldName = config.getString(ConfigNames.LOBBY_WORLD);
            if (lobbyWorldName != null) {
                World world = Bukkit.getWorld(lobbyWorldName);

                if (world != null) {
                    for (ZombiesNPC.ZombiesNPCData zombiesNPCData : zombiesNPCDataList) {
                        zombiesNPCS.add(new ZombiesNPC(world, zombiesNPCData));
                    }
                }
            }
        }
    }

    /*
    Public static utility functions below
     */

    /**
     * Logs a message with this plugin, at the specified level.
     * @param level The level to log at
     * @param message The log message
     */
    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    /**
     * Logs a message with this plugin at Level.INFO
     * @param message The message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a message with this plugin at Level.WARNING
     * @param message The message to log
     */
    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message with this plugin at Level.SEVERE
     * @param message The message to log
     */
    public static void severe(String message) {
        log(Level.SEVERE, message);
    }
}
