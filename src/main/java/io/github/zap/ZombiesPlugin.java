package io.github.zap;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.command.DebugCommand;
import io.github.zap.config.ValidatingConfiguration;
import io.github.zap.event.player.PlayerRightClickEvent;
import io.github.zap.game.arena.Arena;
import io.github.zap.game.Ticker;
import io.github.zap.game.arena.ArenaManager;
import io.github.zap.game.arena.ZombiesArenaManager;
import io.github.zap.game.data.*;
import io.github.zap.world.WorldLoader;
import io.github.zap.proxy.MythicMobs_v4_10_R1;
import io.github.zap.proxy.MythicProxy;
import io.github.zap.net.BungeeHandler;
import io.github.zap.net.NetworkFlow;
import io.github.zap.proxy.SlimeProxy;
import io.github.zap.proxy.SlimeWorldManager_v2_3_R0;
import io.github.zap.serialize.BukkitDataLoader;
import io.github.zap.serialize.DataLoader;

import com.grinderwolf.swm.api.SlimePlugin;

import io.github.zap.world.SlimeWorldLoader;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.util.ChannelNames;
import io.github.zap.util.ConfigNames;
import io.github.zap.util.ConverterNames;
import io.github.zap.util.PluginNames;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;

import org.apache.commons.lang3.Range;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import lombok.Getter;

import java.util.*;
import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin implements Listener {
    @Getter
    private static ZombiesPlugin instance; //singleton for our main plugin class

    @Getter
    private ValidatingConfiguration configuration; //wrapper for bukkit's config manager

    @Getter
    private DataLoader dataLoader; //used to save/load data from custom serialization framework

    @Getter
    private SlimeProxy slimeProxy; //access SWM through this proxy interface

    @Getter
    private MythicProxy mythicProxy; //access mythicmobs through this proxy interface

    @Getter
    private WorldLoader worldLoader; //responsible for loading slime worlds

    @Getter
    private CommandManager commandManager;

    @Getter
    private ArenaManager<? extends Arena> arenaManager;

    @Getter
    private Ticker ticker;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this,this);

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            StopWatch timer = StopWatch.createStarted();

            initConfig();
            initProxies();
            initWorldLoader();
            initSerialization();
            initCommands();
            initMessaging();

            timer.stop();

            getLogger().log(Level.INFO, String.format("Done enabling; ~%sms elapsed", timer.getTime()));
        }
        catch(IllegalStateException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
        }
    }

    @Override
    public void onDisable() {
        //perform shutdown tasks
        PlayerInteractEvent.getHandlerList().unregister((Listener) this);

        ticker.stop();
        getLogger().info("Closing active arenas.");

        StopWatch timer = StopWatch.createStarted();
        for(Arena arena : arenaManager.getArenas()) {
            arena.close();
        }
        timer.stop();

        getLogger().info(String.format("Done closing arenas; ~%sms elapsed", timer.getTime()));
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
        Range<Integer> gametickDelayRange = Range.between(0, 5);
        Range<Integer> arenaTimeoutDelay = Range.between(0, Integer.MAX_VALUE);

        configuration.registerValidator(ConfigNames.MAX_WORLDS, maxWorldRange::contains);
        configuration.registerValidator(ConfigNames.GAMETICK_DELAY, gametickDelayRange::contains);
        configuration.registerValidator(ConfigNames.ARENA_TIMEOUT_DELAY, arenaTimeoutDelay::contains);

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.GAMETICK_DELAY, 2);
        config.addDefault(ConfigNames.ARENA_TIMEOUT_DELAY, 300000);
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initProxies() {
        PluginManager manager = Bukkit.getPluginManager();
        Plugin mythicMobs = manager.getPlugin(PluginNames.MYTHIC_MOBS);
        Plugin swm = manager.getPlugin(PluginNames.SLIME_WORLD_MANAGER);

        if(mythicMobs != null) {
            String mythicVersion = mythicMobs.getDescription().getVersion().split("-")[0];

            //noinspection SwitchStatementWithTooFewBranches
            switch(mythicVersion) {
                case "4.10.1":
                    mythicProxy = new MythicMobs_v4_10_R1((MythicMobs)mythicMobs);
                    break;
                default:
                    throw new IllegalStateException(String.format("Unrecognized MythicMobs version '%s'", mythicVersion));
            }
        }
        else {
            throw new IllegalStateException("Unable to locate required plugin MythicMobs");
        }

        if(swm != null) {
            String swmVersion = swm.getDescription().getVersion().split("-")[0];

            //noinspection SwitchStatementWithTooFewBranches
            switch (swmVersion) {
                case "2.3.0":
                    slimeProxy = new SlimeWorldManager_v2_3_R0((SlimePlugin)swm);
                    break;
                default:
                    throw new IllegalStateException(String.format("Unrecognized SWM version '%s'", swmVersion));
            }
        }
        else {
            throw new IllegalStateException("Unable to locate required plugin SlimeWorldManager");

        }
    }

    private void initWorldLoader() {
        //initialize the arenamanager with the configured maximum default amount of worlds
        arenaManager = new ZombiesArenaManager(configuration.get(ConfigNames.MAX_WORLDS, 10),
                configuration.get(ConfigNames.ARENA_TIMEOUT_DELAY, 300000));
        worldLoader = new SlimeWorldLoader(slimeProxy.getLoader("file"));

        ticker = new Ticker();
        ticker.start(configuration.get(ConfigNames.GAMETICK_DELAY, 2));

        getLogger().info("Preloading worlds.");

        StopWatch timer = StopWatch.createStarted();
        worldLoader.preloadWorlds("world_copy");
        timer.stop();

        getLogger().info(String.format("Done preloading worlds; ~%sms elapsed", timer.getTime()));
    }

    private void initMessaging() {
        registerChannel(new BungeeHandler(), ChannelNames.BUNGEECORD, NetworkFlow.BIDIRECTIONAL);
    }

    private void initSerialization() {
        /*
        include all classes you want to be serialized as arguments to BukkitDataLoader
        (it uses a reflection hack to make ConfigurationSerialization behave in a way that is not completely stupid)
         */

        //noinspection unchecked
        dataLoader = new BukkitDataLoader(DoorData.class, DoorSide.class, MapData.class, RoomData.class,
                ShopData.class, SpawnpointData.class, WindowData.class);

        //register converters
        DataSerializable.registerConverter(ConverterNames.MYTHIC_MOB_SET_CONVERTER, (data, serializing) -> {
            if(serializing) {
                //noinspection unchecked
                Set<MythicMob> set = (Set<MythicMob>)data;
                Set<String> result = new HashSet<>();

                for(MythicMob mob : set) {
                    result.add(mob.getInternalName());
                }

                return result;
            }
            else {
                //noinspection unchecked
                Set<String> set = (Set<String>)data;
                Set<MythicMob> result = new HashSet<>();

                for(String name : set) {
                    result.add(MythicMobs.inst().getAPIHelper().getMythicMob(name));
                }

                return result;
            }
        });
    }

    private void initCommands() {
        commandManager = new CommandManager(this);

        //register commands here
        commandManager.registerCommand(new DebugCommand());
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if(event.getHand() == EquipmentSlot.HAND && (action == Action.RIGHT_CLICK_BLOCK ||
                action == Action.RIGHT_CLICK_AIR)) {
            getServer().getPluginManager().callEvent(new PlayerRightClickEvent(event.getPlayer(),
                    event.getClickedBlock(), event.getItem(), action));
        }
    }
}