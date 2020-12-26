package io.github.zap.zombies;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.loaders.file.FileLoader;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.playerdata.FilePlayerDataManager;
import io.github.zap.arenaapi.playerdata.PlayerDataManager;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.JacksonDataLoader;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.command.DebugCommand;
import io.github.zap.zombies.game.ZombiesArenaManager;
import io.github.zap.zombies.game.data.equipment.EquipmentCreator;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentDeserializer;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.skill.SkillData;
import io.github.zap.zombies.game.data.level.GunLevel;
import io.github.zap.zombies.game.data.level.MeleeLevel;
import io.github.zap.zombies.game.data.level.PerkLevel;
import io.github.zap.zombies.game.data.level.SkillLevel;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.LinearGun;
import io.github.zap.zombies.game.equipment.melee.MeleeWeapon;
import io.github.zap.zombies.game.equipment.perk.PerkEquipment;
import io.github.zap.zombies.game.equipment.skill.SkillEquipment;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.github.zap.zombies.proxy.ZombiesNMSProxy_v1_16_R3;
import io.github.zap.zombies.world.SlimeWorldLoader;
import io.lumine.xikage.mythicmobs.MythicMobs;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class Zombies extends JavaPlugin implements Listener {
    @Getter
    private static Zombies instance; //singleton for our main plugin class

    @Getter
    private ZombiesNMSProxy nmsProxy;

    @Getter
    private ArenaApi arenaApi;

    @Getter
    private SWMPlugin SWM; //access SWM through this proxy interface

    @Getter
    private File slimeWorldDirectory;

    @Getter
    private String slimeExtension;

    @Getter
    private SlimeLoader slimeLoader;

    @Getter
    private MythicMobs mythicMobs; ///access mythicmobs through this proxy interface

    @Getter
    private DataLoader dataLoader; //used to save/load data from custom serialization framework

    @Getter
    private WorldLoader worldLoader; //responsible for loading worlds

    @Getter
    private CommandManager commandManager;

    @Getter
    private PlayerDataManager playerDataManager;

    @Getter
    private LocalizationManager localizationManager;

    @Getter
    private EquipmentManager equipmentManager;

    public static final String DEFAULT_LOCALE = "en_US";
    public static final String ARENA_METADATA_NAME = "zombies_arena";
    public static final String LOCALIZATION_FOLDER_NAME = "localization";
    public static final String MAP_FOLDER_NAME = "maps";
    public static final String EQUIPMENT_FOLDER_NAME = "equipments";
    public static final String PLAYER_DATA_FOLDER_NAME = "playerdata";

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            initConfig();
            initProxy();
            initDependencies();
            initSerialization();
            initPlayerDataManager();
            initLocalization();
            initWorldLoader();
            initArenaManagers();
            initCommands();
        }
        catch(LoadFailureException exception)
        {
            severe(String.format("A fatal error occured that prevented the plugin from enabling properly: '%s'.",
                    exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
            return;
        }

        timer.stop();
        info(String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    @Override
    public void onDisable() {
        playerDataManager.flushAll(); //ensures any unsaved playerdata is saved when the plugin shuts down
    }

    private void initConfig() {
        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.addDefault(ConfigNames.DATA_CACHE_CAPACITY, 2048);
        config.addDefault(ConfigNames.DEFAULT_LOCALE, DEFAULT_LOCALE);
        config.addDefault(ConfigNames.LOCALIZATION_DIRECTORY, Path.of(getDataFolder().getPath(),
                LOCALIZATION_FOLDER_NAME).toFile().getPath());
        config.addDefault(ConfigNames.WORLD_SPAWN, new Vector(0, 1, 0));
        config.addDefault(ConfigNames.LOBBY_WORLD,"world");

        config.options().copyDefaults(true);
        saveConfig();
    }

    private void initProxy() throws LoadFailureException {
        //noinspection SwitchStatementWithTooFewBranches
        switch (Bukkit.getBukkitVersion()) {
            case "1.16.4-R0.1-SNAPSHOT":
                nmsProxy = new ZombiesNMSProxy_v1_16_R3();
                break;
            default:
                throw new LoadFailureException(String.format("Unsupported MC version '%s'.", Bukkit.getBukkitVersion()));
        }
    }

    private void initDependencies() throws LoadFailureException {
        arenaApi = ArenaApi.getRequiredPlugin(PluginNames.ARENA_API, true);
        SWM = ArenaApi.getRequiredPlugin(PluginNames.SLIME_WORLD_MANAGER, true);
        mythicMobs = ArenaApi.getRequiredPlugin(PluginNames.MYTHIC_MOBS, true);
    }

    private void initWorldLoader() {
        info("Preloading worlds.");

        StopWatch timer = StopWatch.createStarted();
        slimeWorldDirectory = new File("slime_worlds");
        slimeExtension = ".slime";
        slimeLoader = new FileLoader(slimeWorldDirectory);
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
                ZombiesArenaManager zombiesArenaManager = new ZombiesArenaManager(WorldUtils.locationFrom(world, spawn),
                        Path.of(getDataFolder().getPath(), MAP_FOLDER_NAME).toFile(), config.getInt(ConfigNames.MAX_WORLDS),
                        config.getInt(ConfigNames.ARENA_TIMEOUT));
                arenaApi.registerArenaManager(zombiesArenaManager);
            }
            else {
                throw new LoadFailureException(String.format("Specified lobby world '%s' does not exist.", worldName));
            }
        }
        else {
            throw new LoadFailureException("Unable to load required configuration information for ZombiesArenaManager.");
        }
    }

    private void initSerialization() throws LoadFailureException {
        equipmentManager = new EquipmentManager(Path.of(getDataFolder().getPath(), EQUIPMENT_FOLDER_NAME).toFile());

        Map<String, EquipmentCreator.EquipmentMapping<?, ?>> equipmentMappings = equipmentManager.getEquipmentCreator().getEquipmentMappings();
        equipmentMappings.put(EquipmentType.MELEE.toString(), (EquipmentCreator.EquipmentMapping<MeleeData, MeleeLevel>) MeleeWeapon::new);
        equipmentMappings.put(EquipmentType.SKILL.toString(), (EquipmentCreator.EquipmentMapping<SkillData, SkillLevel>) SkillEquipment::new);
        equipmentMappings.put(EquipmentType.PERK.toString(), (EquipmentCreator.EquipmentMapping<PerkData, PerkLevel>) PerkEquipment::new);
        equipmentMappings.put(EquipmentType.LINEAR_GUN.toString(), (EquipmentCreator.EquipmentMapping<LinearGunData, GunLevel>) LinearGun::new);

        Map<String, Class<? extends EquipmentData<?>>> equipmentClassMappings = equipmentManager.getEquipmentDeserializer().getEquipmentClassMappings();
        equipmentClassMappings.put(EquipmentType.MELEE.toString(), MeleeData.class);
        equipmentClassMappings.put(EquipmentType.SKILL.toString(), SkillData.class);
        equipmentClassMappings.put(EquipmentType.PERK.toString(), PerkData.class);
        equipmentClassMappings.put(EquipmentType.LINEAR_GUN.toString(), LinearGunData.class);

        dataLoader = new JacksonDataLoader(new SimpleModule() {
            {
                addDeserializer(EquipmentData.class, equipmentManager.getEquipmentDeserializer());
            }
        });
    }

    private void initPlayerDataManager() {
        playerDataManager = new FilePlayerDataManager(Path.of(getDataFolder().getPath(), PLAYER_DATA_FOLDER_NAME)
                .toFile(), dataLoader, getConfig().getInt(ConfigNames.DATA_CACHE_CAPACITY));
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
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(new DebugCommand());
    }

    /*
    Public static utility functions below
     */

    /**
     * Sends a localized message to the specific player. The message displayed will be in whatever language the
     * player has chosen.
     * @param player The player we are sending the message to
     * @param key The MessageKey of the message we are sending
     * @param formatArgs Format arguments for the message, which may be necessary for some MessageKeys
     */
    public static void sendLocalizedMessage(Player player, MessageKey key, Object... formatArgs) {
        instance.getLocalizationManager().sendLocalizedMessage(player, key.getKey(), formatArgs);
    }

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

    /**
     * Calls the specified event for this plugin.
     * @param event The event to call
     */
    public static void callEvent(Event event) {
        instance.getServer().getPluginManager().callEvent(event);
    }
}
