package io.github.zap.zombies;

import com.google.common.collect.Lists;
import com.grinderwolf.swm.api.SlimePlugin;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.serialize.BukkitDataLoader;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.ValueConverter;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.command.DebugCommand;
import io.github.zap.zombies.game.ZombiesArenaManager;
import io.github.zap.zombies.game.data.*;
import io.github.zap.zombies.proxy.MythicMobs_v4_10_R1;
import io.github.zap.zombies.proxy.MythicProxy;
import io.github.zap.zombies.proxy.SlimeProxy;
import io.github.zap.zombies.proxy.SlimeWorldManager_v2_3_R0;
import io.github.zap.zombies.world.SlimeWorldLoader;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
    private DataLoader dataLoader; //used to save/load data from custom serialization framework

    @Getter
    private SlimeProxy slimeProxy; //access SWM through this proxy interface

    @Getter
    private MythicProxy mythicProxy; //access mythicmobs through this proxy interface

    @Getter
    private WorldLoader worldLoader; //responsible for loading slime worlds

    @Getter
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        arenaApi = (ArenaApi)Bukkit.getPluginManager().getPlugin(PluginNames.ARENA_API);

        try {
            //put plugin enabling code below. throw IllegalStateException if something goes wrong and we need to abort
            StopWatch timer = StopWatch.createStarted();

            initConfig();
            initCommands();
            initSerialization();
            initProxies();
            initWorldLoader();
            initArenaManagers();

            timer.stop();

            getLogger().log(Level.INFO, String.format("Done enabling; ~%sms elapsed", timer.getTime()));
        }
        catch(LoadFailureException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
        }
    }

    @Override
    public void onDisable() {
        //perform shutdown tasks
    }

    private void initConfig() {
        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initWorldLoader() {
        getLogger().info("Preloading worlds.");
        StopWatch timer = StopWatch.createStarted();
        worldLoader = new SlimeWorldLoader(slimeProxy.getLoader("file"));
        worldLoader.preload();
        timer.stop();

        getLogger().info(String.format("Done preloading worlds; ~%sms elapsed", timer.getTime()));
    }

    private void initArenaManagers() {
        FileConfiguration config = getConfig();
        ZombiesArenaManager zombiesArenaManager = new ZombiesArenaManager(new File(String.format("plugins/%s/maps",
                getName())), config.getInt(ConfigNames.MAX_WORLDS), config.getInt(ConfigNames.ARENA_TIMEOUT));

    }

    private void initProxies() throws LoadFailureException {
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
                    throw new LoadFailureException(String.format("Unrecognized MythicMobs version '%s'", mythicVersion));
            }
        }
        else {
            throw new LoadFailureException("Unable to locate required plugin MythicMobs");
        }

        if(swm != null) {
            String swmVersion = swm.getDescription().getVersion().split("-")[0];

            //noinspection SwitchStatementWithTooFewBranches
            switch (swmVersion) {
                case "2.3.0":
                    slimeProxy = new SlimeWorldManager_v2_3_R0((SlimePlugin)swm);
                    break;
                default:
                    throw new LoadFailureException(String.format("Unrecognized SWM version '%s'", swmVersion));
            }
        }
        else {
            throw new LoadFailureException("Unable to locate required plugin SlimeWorldManager");

        }
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
        commandManager = new CommandManager(this);

        //register commands here
        commandManager.registerCommand(new DebugCommand());
    }
}