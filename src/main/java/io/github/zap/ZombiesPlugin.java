package io.github.zap;

import io.github.zap.config.ValidatingConfiguration;
import io.github.zap.manager.ArenaManager;
import io.github.zap.data.*;
import io.github.zap.net.BungeeHandler;
import io.github.zap.net.NetworkFlow;
import io.github.zap.serialize.BukkitDataLoader;
import io.github.zap.serialize.ConverterNames;
import io.github.zap.serialize.DataLoader;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.swm.SlimeMapLoader;

import com.grinderwolf.swm.api.SlimePlugin;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.StopWatch;

import org.apache.commons.lang3.Range;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import lombok.Getter;

import java.util.Objects;
import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin {
    @Getter
    private static ZombiesPlugin instance; //singleton pattern for our main plugin class

    @Getter
    private ValidatingConfiguration configuration; //access the plugin config through this wrapper class

    @Getter
    private DataLoader dataLoader;

    @Getter
    private SlimePlugin slimePlugin;

    @Getter
    private SlimeMapLoader slimeMapLoader;

    /*
    Warning! This object is NOT thread safe! Only call if you're on the main server thread. Also make sure you always
    call timer.reset() after a call to timer.start() (use a finally block in case of exceptions!)
    */
    @Getter
    private StopWatch timer;

    /*
    the ArenaManager is responsible for adding players to games, or sending them to other servers if we're using bungee
     */
    @Getter
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        instance = this;
        timer = new StopWatch();

        try {
            timer.start();
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort

            initConfig();

            //initialize the arenamanager with the configured maximum default amount of worlds
            arenaManager = new ArenaManager(configuration.get(ConfigConstants.MAX_WORLDS, 10));

            initMessaging();
            initSlimeMapLoader();
            initSerialization();

            timer.stop();
            getLogger().log(Level.INFO, String.format("Done enabling: ~%sms", timer.getTime()));
        }
        catch(IllegalStateException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling. Reason: \"%s\"", exception.getMessage()));
            getPluginLoader().disablePlugin(this, true);
        }
        finally { //ensure profiler gets reset
            timer.reset();
        }
    }

    @Override
    public void onDisable() {
        //perform shutdown tasks
    }

    /**
     * Registers a channel and potentially supplies a ChannelHandler for that channel. The latter is only performed
     * if NetworkFlow is set to either INCOMING or BIDIRECTIONAL. When registering a NetworkFlow.OUTGOING, handler
     * MUST be null. When registering BIDIRECTIONAL or INCOMING, it must NOT be null.
     * @param handler The handler to register
     * @param channel The channel to open, and potentially register a handler for
     * @param flow Whether or not to open outgoing, incoming, or both plugin channels
     */
    public void registerChannel(PluginMessageListener handler, String channel, NetworkFlow flow) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(flow, "flow cannot be null");

        Validate.isTrue((flow == NetworkFlow.OUTGOING) == (handler == null),
                "the specified NetworkFlow is not valid given the other arguments");

        Messenger messenger = getServer().getMessenger();

        switch(flow) {
            case INCOMING:
                messenger.registerIncomingPluginChannel(this, channel, handler);
                break;
            case OUTGOING:
                messenger.registerOutgoingPluginChannel(this, channel);
                break;
            case BIDIRECTIONAL:
                messenger.registerIncomingPluginChannel(this, channel, handler);
                messenger.registerOutgoingPluginChannel(this, channel);
                break;
        }
    }

    private void initConfig() {
        FileConfiguration config = getConfig();
        configuration = new ValidatingConfiguration(config);

        //make sure the MAX_WORLDS config var is within a reasonable range
        Range<Integer> maxWorldRange = Range.between(1, 64);
        configuration.registerValidator(ConfigConstants.MAX_WORLDS, maxWorldRange::contains);

        config.addDefault(ConfigConstants.MAX_WORLDS, 10);
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void initSlimeMapLoader() {
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if(slimePlugin != null) {
            slimeMapLoader = new SlimeMapLoader(slimePlugin);
        }
        else { //plugin should never be null because it's a dependency, but it's best to be safe
            super.getPluginLoader().disablePlugin(this);
            throw new IllegalStateException("Unable to locate required plugin SlimeWorldManager.");
        }
    }

    private void initMessaging() {
        registerChannel(new BungeeHandler(), ChannelNames.BUNGEECORD, NetworkFlow.BIDIRECTIONAL);
    }

    private void initSerialization() {
        ConfigurationSerialization.registerClass(DataSerializable.class);

        /*
        include all classes you want to be serialized as arguments to BukkitDataLoader
        (it uses a reflection hack to make ConfigurationSerialization behave in a way that is not completely stupid)
         */

        //noinspection unchecked
        dataLoader = new BukkitDataLoader(DoorData.class, MapData.class, MultiBoundingBox.class, RoomData.class,
                WindowData.class);
    }
}