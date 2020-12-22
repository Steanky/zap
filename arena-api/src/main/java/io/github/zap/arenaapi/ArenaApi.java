package io.github.zap.arenaapi;

import com.comphenix.protocol.ProtocolLib;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.proxy.NMSProxy;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class ArenaApi extends JavaPlugin {
    @Getter
    private static ArenaApi instance;

    @Getter
    private NMSProxy nmsProxy;

    @Getter
    private ProtocolLib protocolLib;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            initProxy();
            initDependencies();
        }
        catch(LoadFailureException exception)
        {
            severe(String.format("A fatal error occured that prevented the plugin from enabling properly: '%s'.",
                    exception.getMessage()));
            getPluginLoader().disablePlugin(this, true);
            return;
        }

        timer.stop();
        getLogger().info(String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }


    private void initProxy() throws LoadFailureException {
        //noinspection SwitchStatementWithTooFewBranches
        switch (Bukkit.getBukkitVersion()) {
            case "1.16.4-R0.1-SNAPSHOT":
                nmsProxy = new NMSProxy_v1_16_R3();
                break;
            default:
                throw new LoadFailureException(String.format("Unsupported MC version '%s'.", Bukkit.getBukkitVersion()));
        }
    }

    private void initDependencies() throws LoadFailureException {
        protocolLib = getRequiredPlugin(PluginNames.PROTOCOL_LIB, true);
    }

    public void registerArenaManager(ArenaManager<?> manager) {
        Validate.notNull(manager, "manager cannot be null");
        Validate.isTrue(arenaManagers.putIfAbsent(manager.getGameName(), manager) == null,
                "a manager for game type '%s' has already been registered");
    }

    public void handleJoin(JoinInformation information, Consumer<ImmutablePair<Boolean, String>> onCompletion) {
        String gameName = information.getGameName();
        ArenaManager<?> arenaManager = arenaManagers.get(gameName);

        if(arenaManager != null) {
            arenaManager.handleJoin(information, onCompletion);
        }
        else {
            warning(String.format("Invalid JoinInformation received: '%s' is not a game.", gameName));
        }
    }

    /*
    Static utility functions below
     */

    public static <T extends Plugin> T getRequiredPlugin(String pluginName, boolean requireEnabled)
            throws LoadFailureException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if(plugin != null) {
            if(plugin.isEnabled() || !requireEnabled) {
                try {
                    //noinspection unchecked
                    return (T)plugin;
                }
                catch (ClassCastException ignored) {
                    throw new LoadFailureException(String.format("ClassCastException when loading plugin %s.", pluginName));
                }
            }
            else {
                throw new LoadFailureException(String.format("Plugin %s is not enabled.", pluginName));
            }
        }
        else {
            throw new LoadFailureException(String.format("Required plugin %s cannot be found.", pluginName));
        }
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
     * Logs a message with this plugin at Level.INFO — to signify that things are happening which are normal, expected
     * operations.
     * @param message The message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a message with this plugin at Level.WARNING — signifying that something happened which has potential to
     * cause issues but has not resulted in a hard crash.
     * @param message The message to log
     */
    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message with this plugin at Level.SEVERE — signifying that something happened which caused a hard crash.
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