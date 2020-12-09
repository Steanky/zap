package io.github.zap.arenaapi;

import com.comphenix.protocol.ProtocolLib;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.proxy.NMSProxy;
import io.github.zap.arenaapi.proxy.NMSUtilProxy_v1_16_R3;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class ArenaApi extends JavaPlugin {
    @Getter
    private static ArenaApi instance;

    @Getter
    private NMSProxy nmsProxy;

    @Getter
    private ProtocolLib protocolLib;

    @Getter
    private LocalizationManager localizationManager;

    @Getter
    private CommandManager commandManager;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            initConfig();
            initProxy();
            initDependencies();
            initLocalization();
            initCommands();
        }
        catch(LoadFailureException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, true);
            return;
        }

        timer.stop();
        getLogger().info(String.format("Enabled successfully; ~%sms elapsed", timer.getTime()));
    }

    private void initConfig() {
        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.DEFAULT_LOCALE, "en_US");
        config.addDefault(ConfigNames.LOCALIZATION_DIRECTORY, "localization");
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initProxy() throws LoadFailureException {
        //noinspection SwitchStatementWithTooFewBranches
        switch (Bukkit.getBukkitVersion()) {
            case "1.16.4-R0.1-SNAPSHOT":
                nmsProxy = new NMSUtilProxy_v1_16_R3();
                break;
            default:
                throw new LoadFailureException(String.format("Unsupported MC version '%s'", Bukkit.getBukkitVersion()));
        }
    }

    private void initDependencies() throws LoadFailureException {
        protocolLib = getRequiredPlugin(PluginNames.PROTOCOL_LIB);
    }

    private void initLocalization() throws LoadFailureException {
        Configuration config = getConfig();

        String locale = config.getString(ConfigNames.DEFAULT_LOCALE);
        String localizationDirectory = config.getString(ConfigNames.LOCALIZATION_DIRECTORY);

        if(locale != null && localizationDirectory != null) {
            localizationManager = new LocalizationManager(Locale.forLanguageTag(locale),
                    new File(localizationDirectory));
        }
        else {
            throw new LoadFailureException("One or more configuration entries could not be retrieved.");
        }
    }

    private void initCommands() {
        commandManager = new CommandManager(this);
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
            getLogger().warning(String.format("Invalid JoinInformation received: '%s' is not a game", gameName));
        }
    }

    public static <T extends Plugin> T getRequiredPlugin(String pluginName)
            throws LoadFailureException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if(plugin != null) {
            try {
                //noinspection unchecked
                return (T)plugin;
            }
            catch (ClassCastException ignored) {
                throw new LoadFailureException(String.format("ClassCastException when loading plugin %s", pluginName));
            }
        }
        else {
            throw new LoadFailureException(String.format("Required plugin %s cannot be found.", pluginName));
        }
    }
}