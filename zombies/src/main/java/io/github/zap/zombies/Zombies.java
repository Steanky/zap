package io.github.zap.zombies;

import com.google.common.collect.Lists;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.loaders.file.FileLoader;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.event.CustomEvent;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.serialize.BukkitDataLoader;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.ValueConverter;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.command.DebugCommand;
import io.github.zap.zombies.game.ZombiesArenaManager;
import io.github.zap.zombies.game.data.*;
import io.github.zap.zombies.world.SlimeWorldLoader;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Zombies extends JavaPlugin implements Listener {
    @Getter
    private static Zombies instance; //singleton for our main plugin class

    @Getter
    private ArenaApi arenaApi;

    @Getter
    private SWMPlugin SWM; //access SWM through this proxy interface

    @Getter
    private SlimeLoader slimeLoader;

    @Getter
    private File slimeWorldDirectory;

    @Getter
    private String slimeExtension;

    @Getter
    private MythicMobs mythicMobs; ///access mythicmobs through this proxy interface

    @Getter
    private DataLoader dataLoader; //used to save/load data from custom serialization framework

    @Getter
    private WorldLoader worldLoader; //responsible for loading worlds

    @Getter
    private CommandManager commandManager;

    @Getter
    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            initConfig();
            initDependencies();
            initSerialization();
            initWorldLoader();
            initArenaManagers();
            initCommands();

            timer.stop();

            info(String.format("Done enabling; ~%sms elapsed.", timer.getTime()));
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

    private void initConfig() {
        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initDependencies() throws LoadFailureException {
        arenaApi = ArenaApi.getRequiredPlugin(PluginNames.ARENA_API, true);
        SWM = ArenaApi.getRequiredPlugin(PluginNames.SLIME_WORLD_MANAGER, true);
        mythicMobs = ArenaApi.getRequiredPlugin(PluginNames.MYTHIC_MOBS, true);
    }

    private void initWorldLoader() {
        info("Preloading worlds.");

        StopWatch timer = StopWatch.createStarted();
        slimeLoader = new FileLoader(slimeWorldDirectory);
        slimeWorldDirectory = new File("slime");
        slimeExtension = ".slime";
        worldLoader = new SlimeWorldLoader(slimeLoader);
        worldLoader.preload();
        timer.stop();

        info(String.format("Done preloading worlds; ~%sms elapsed.", timer.getTime()));
    }

    private void initArenaManagers() {
        FileConfiguration config = getConfig();
        ZombiesArenaManager zombiesArenaManager = new ZombiesArenaManager(new File(String.format("plugins/%s/maps",
                getName())), config.getInt(ConfigNames.MAX_WORLDS), config.getInt(ConfigNames.ARENA_TIMEOUT));
        arenaApi.registerArenaManager(zombiesArenaManager);
    }

    private void initSerialization() throws LoadFailureException {
        /*
        include all classes you want to be serialized as arguments to BukkitDataLoader
        (it uses a reflection hack to make ConfigurationSerialization behave in a way that is not completely stupid)
         */

        dataLoader = new BukkitDataLoader(DoorData.class, DoorSide.class, MapData.class, RoomData.class,
                ShopData.class, SpawnpointData.class, WindowData.class);

        //noinspection rawtypes
        DataSerializable.registerGlobalConverter(Locale.class, ArrayList.class, new ValueConverter<Locale, ArrayList>() {
            @Override
            public ArrayList serialize(Locale object) {
                return Lists.newArrayList(object.getLanguage(), object.getCountry(), object.getVariant());
            }

            @Override
            public Locale deserialize(ArrayList object) {
                return new Locale((String)object.get(0), (String)object.get(1), (String)object.get(2));
            }
        });

        DataSerializable.registerGlobalConverter(MythicMob.class, String.class, new ValueConverter<MythicMob, String>() {
            @Override
            public String serialize(MythicMob object) {
                return object.getInternalName();
            }

            @Override
            public MythicMob deserialize(String object) {
                return MythicMobs.inst().getAPIHelper().getMythicMob(object);
            }
        });
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
     * @param formatArgs Format arguments for the message, which may or may not be necessary for a given format string
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