package io.github.zap;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.command.DebugCommand;
import io.github.zap.config.ValidatingConfiguration;
import io.github.zap.event.PlayerRightClickEvent;
import io.github.zap.localization.LocalizationManager;
import io.github.zap.manager.ArenaManager;
import io.github.zap.maploader.MapLoader;
import io.github.zap.serialize.BukkitDataLoader;
import io.github.zap.serialize.DataLoader;

import com.grinderwolf.swm.api.SlimePlugin;

import io.github.zap.maploader.SlimeMapLoader;
import io.github.zap.util.ConfigNames;
import org.apache.commons.lang3.time.StopWatch;

import org.apache.commons.lang3.Range;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin implements Listener {
    @Getter
    private static ZombiesPlugin instance; //singleton pattern for our main plugin class

    @Getter
    private ValidatingConfiguration configuration; //access the plugin config through this wrapper class

    @Getter
    private DataLoader dataLoader;

    @Getter
    private SlimePlugin slimePlugin;

    @Getter
    private MapLoader mapLoader;

    @Getter
    private LocalizationManager localizationManager;

    @Getter
    private CommandManager commandManager;

    /*
    the ArenaManager is responsible for adding players to games, or sending them to other servers if we're using bungee
     */
    @Getter
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        instance = this;
        StopWatch timer = new StopWatch();

        try {
            timer.start();
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort

            initConfig();
            initSerialization();
            initLocalization();
            initCommands();
            initMapLoader();

            //self register as listener for testing custom event code
            getServer().getPluginManager().registerEvents(this, this);

            timer.stop();
            getLogger().log(Level.INFO, String.format("Done enabling: ~%sms", timer.getTime()));
        }
        catch(IllegalStateException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
        }
        finally { //ensure timer gets reset
            timer.reset();
        }
    }

    @Override
    public void onDisable() {
        //perform shutdown tasks
    }

    private void initConfig() {
        FileConfiguration config = getConfig();
        configuration = new ValidatingConfiguration(config);

        //make sure the MAX_WORLDS config var is within a reasonable range
        Range<Integer> maxWorldRange = Range.between(1, 64);
        configuration.registerValidator(ConfigNames.MAX_WORLDS, maxWorldRange::contains);

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void initLocalization() {
        getLogger().info("Initializing localization");

        StopWatch timer = StopWatch.createStarted();
        localizationManager = new LocalizationManager(Locale.US, new File("localization"));
        localizationManager.loadTranslations();
        timer.stop();

        getLogger().info(String.format("Localization initialized; ~%sms elapsed", timer.getTime()));
    }

    private void initMapLoader() {
        //initialize the arenamanager with the configured maximum default amount of worlds
        arenaManager = new ArenaManager(configuration.get(ConfigNames.MAX_WORLDS, 10));

        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

        if(slimePlugin != null) {
            mapLoader = new SlimeMapLoader(slimePlugin, slimePlugin.getLoader("file"));

            getLogger().info("Preloading worlds.");

            StopWatch timer = new StopWatch();

            try {
                timer.start();
                mapLoader.preloadWorlds();
                timer.stop();

                getLogger().info(String.format("Done preloading worlds; ~%sms elapsed", timer.getTime()));
            }
            finally {
                timer.reset();
            }
        }
        else { //plugin should never be null because it's a dependency, but it's best to be safe
            throw new IllegalStateException("Unable to locate required plugin SlimeWorldManager.");
        }
    }

    private void initSerialization() {
        /*
        include all classes you want to be serialized as arguments to BukkitDataLoader
        (it uses a reflection hack to make ConfigurationSerialization behave in a way that is not completely stupid)
         */

        //noinspection unchecked
        dataLoader = new BukkitDataLoader();
    }

    private void initCommands() {
        commandManager = new CommandManager(this);

        //register commands here
        commandManager.registerCommand(new DebugCommand());
    }

    /*
    example code showing how custom events can work
     */

    @EventHandler
    private void onPlayerRightClick(PlayerRightClickEvent event) {
        getLogger().info("Got PlayerRightClickEvent.");
    }

    /**
     * Example: handle Bukkit onPlayerInteract and invoke our custom PlayerRightClickEvent if the user right-clicked.
     * @param event The PlayerInteractEvent
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        getLogger().info("Got PlayerInteractEvent.");

        Action action = event.getAction();
        if(event.getHand() == EquipmentSlot.HAND && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            Bukkit.getServer().getPluginManager().callEvent(new PlayerRightClickEvent(event.getPlayer(), event.getClickedBlock(), event.getItem(), event.getAction()));
        }
    }
}