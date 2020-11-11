package io.github.zap;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.sun.istack.internal.NotNull;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.command.DebugCommand;
import io.github.zap.config.ValidatingConfiguration;
import io.github.zap.game.arena.Arena;
import io.github.zap.game.arena.ArenaManager;
import io.github.zap.game.arena.JoinInformation;
import io.github.zap.game.arena.ZombiesArenaManager;
import io.github.zap.game.data.*;
import io.github.zap.util.ChannelNames;
import io.github.zap.world.WorldLoader;
import io.github.zap.proxy.MythicMobs_v4_10_R1;
import io.github.zap.proxy.MythicProxy;
import io.github.zap.proxy.SlimeProxy;
import io.github.zap.proxy.SlimeWorldManager_v2_3_R0;
import io.github.zap.serialize.BukkitDataLoader;
import io.github.zap.serialize.DataLoader;

import com.grinderwolf.swm.api.SlimePlugin;

import io.github.zap.world.SlimeWorldLoader;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.util.ConfigNames;
import io.github.zap.util.PluginNames;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.apache.commons.lang3.time.StopWatch;

import org.apache.commons.lang3.Range;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.*;
import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin implements Listener, PluginMessageListener {
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

    private Map<String, ArenaManager<? extends Arena<?>>> arenaManagerMappings = new HashMap<>();
    private Collection<ArenaManager<? extends Arena<?>>> arenaManagers = arenaManagerMappings.values();

    @Override
    public void onEnable() {
        instance = this;

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            StopWatch timer = StopWatch.createStarted();

            initConfig();
            initProxies();
            initSerialization();
            initWorldLoader();
            initArenaManagers();
            initCommands();
            initNetworking();

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
        getLogger().info("Shutting down ArenaManagers.");

        StopWatch timer = StopWatch.createStarted();
        for(ArenaManager<?> manager : arenaManagers) {
            manager.shutdown();
        }
        timer.stop();

        getLogger().info(String.format("Done shutting down ArenaManagers; ~%sms elapsed", timer.getTime()));
    }

    /**
     * Returns the named ArenaManager, or null if none exist.
     * @param name The name of the ArenaManager to fetch
     * @return The ArenaManager, or null
     */
    public ArenaManager<?> getArenaManager(String name) {
        return arenaManagerMappings.get(name);
    }

    /**
     * Adds the specified ArenaManager to the plugin.
     * @param manager The ArenaManager to add
     */
    public void addArenaManager(ArenaManager<?> manager) {
        arenaManagerMappings.put(manager.getName(), manager);
    }

    private void initConfig() {
        FileConfiguration config = getConfig();
        configuration = new ValidatingConfiguration(config);

        //make sure the MAX_WORLDS config var is within a reasonable range
        Range<Integer> maxWorldRange = Range.between(1, 64);
        Range<Integer> arenaTimeoutDelay = Range.between(0, Integer.MAX_VALUE);

        configuration.registerValidator(ConfigNames.MAX_WORLDS, maxWorldRange::contains);
        configuration.registerValidator(ConfigNames.ARENA_TIMEOUT, arenaTimeoutDelay::contains);

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initWorldLoader() {
        worldLoader = new SlimeWorldLoader(slimeProxy.getLoader("file"));

        getLogger().info("Preloading worlds.");

        StopWatch timer = StopWatch.createStarted();
        worldLoader.preloadWorlds();
        timer.stop();

        getLogger().info(String.format("Done preloading worlds; ~%sms elapsed", timer.getTime()));
    }

    private void initArenaManagers() {
        addArenaManager(new ZombiesArenaManager(configuration.get(ConfigNames.MAX_WORLDS, 10),
                configuration.get(ConfigNames.ARENA_TIMEOUT, 300000)));
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

    private void initSerialization() {
        /*
        include all classes you want to be serialized as arguments to BukkitDataLoader
        (it uses a reflection hack to make ConfigurationSerialization behave in a way that is not completely stupid)
         */

        //noinspection unchecked
        dataLoader = new BukkitDataLoader(DoorData.class, DoorSide.class, MapData.class, RoomData.class,
                ShopData.class, SpawnpointData.class, WindowData.class);

        DataSerializable.registerGlobalConverter(Locale.class, ArrayList.class, (object, serializing) -> {
            if(serializing) {
                Locale locale = (Locale)object;
                return Lists.newArrayList(locale.getLanguage(), locale.getCountry(), locale.getVariant());
            }
            else {
                List<?> components = (List<?>)object;
                return new Locale((String)components.get(0), (String)components.get(1), (String)components.get(2));
            }
        });

        DataSerializable.registerGlobalConverter(MythicMob.class, String.class, (object, serializing) -> {
            if(serializing) {
                return ((MythicMob)object).getInternalName();
            }
            else {
                return MythicMobs.inst().getAPIHelper().getMythicMob((String)object);
            }
        });
    }

    private void initCommands() {
        commandManager = new CommandManager(this);

        //register commands here
        commandManager.registerCommand(new DebugCommand());
    }

    private void initNetworking() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, ChannelNames.BUNGEECORD);
        getServer().getMessenger().registerIncomingPluginChannel(this, ChannelNames.BUNGEECORD, this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals(ChannelNames.BUNGEECORD)) {
            ByteArrayDataInput input = ByteStreams.newDataInput(message);
            String subchannel = input.readUTF();

            if (subchannel.equals("Forward")) {
                input.readUTF(); //disregard target server
                String packetName = input.readUTF();

                if (packetName.equals("JOIN")) {
                    input.readUTF();
                    String playerString = input.readUTF();
                    Player[] players = Arrays.stream(playerString.split(",")).map((name) ->
                            getServer().getPlayer(UUID.fromString(name))).toArray(Player[]::new);

                    if (Arrays.stream(players).allMatch((player1 -> player1 != null && player1.isOnline()))) {
                        String managerName = input.readUTF();
                        String mapName = input.readUTF();
                        boolean isSpectator = input.readBoolean();
                        String targetArena = input.readUTF();

                        ArenaManager<?> arenaManager = arenaManagerMappings.get(managerName);
                        if (arenaManager != null) {
                            arenaManager.handleJoin(new JoinInformation(Arrays.asList(players), mapName, isSpectator,
                                    targetArena), (pair) -> {
                                if (!pair.left) {
                                    //send error message to player
                                }
                            });
                        } else {

                        }
                    }
                }
            }
        }
    }
}