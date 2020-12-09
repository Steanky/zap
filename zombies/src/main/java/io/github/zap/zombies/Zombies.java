package io.github.zap.zombies;

import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Lists;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.loaders.file.FileLoader;
import io.github.regularcommands.commands.CommandManager;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.proxy.NMSProxy;
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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Zombies extends JavaPlugin implements Listener {
    @Getter
    private static Zombies instance; //singleton for our main plugin class

    @Getter
    private DataLoader dataLoader; //used to save/load data from custom serialization framework

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
    private MythicMobs mythicMobs; //access mythicmobs through this proxy interface

    @Getter
    private WorldLoader worldLoader; //responsible for loading slime worlds

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            initConfig();
            initDependencies();
            initSerialization();
            initWorldLoader();
            initArenaManagers();
            initCommands();
        }
        catch(LoadFailureException exception)
        {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
            return;
        }

        timer.stop();
        getLogger().log(Level.INFO, String.format("Enabled successfully; ~%sms elapsed", timer.getTime()));
    }

    private void initConfig() {
        FileConfiguration config = getConfig();

        config.addDefault(ConfigNames.MAX_WORLDS, 10);
        config.addDefault(ConfigNames.ARENA_TIMEOUT, 300000);
        config.options().copyDefaults(true);

        saveConfig();
    }

    private void initDependencies() throws LoadFailureException {
        arenaApi = ArenaApi.getRequiredPlugin(PluginNames.ARENA_API);
        SWM = ArenaApi.getRequiredPlugin(PluginNames.SLIME_WORLD_MANAGER);
        mythicMobs = ArenaApi.getRequiredPlugin(PluginNames.MYTHIC_MOBS);
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

    private void initWorldLoader() {
        getLogger().info("Preloading worlds.");

        StopWatch timer = StopWatch.createStarted();
        slimeLoader = new FileLoader(slimeWorldDirectory);
        slimeWorldDirectory = new File("slime");
        slimeExtension = ".slime";
        worldLoader = new SlimeWorldLoader(slimeLoader);
        worldLoader.preload();
        timer.stop();

        getLogger().info(String.format("Done preloading worlds; ~%sms elapsed", timer.getTime()));
    }

    private void initArenaManagers() {
        FileConfiguration config = getConfig();
        ZombiesArenaManager zombiesArenaManager = new ZombiesArenaManager(new File(Directories.MAPS),
                config.getInt(ConfigNames.MAX_WORLDS), config.getInt(ConfigNames.ARENA_TIMEOUT));
        arenaApi.registerArenaManager(zombiesArenaManager);
    }

    private void initCommands() {
        CommandManager commandManager = arenaApi.getCommandManager();
        commandManager.registerCommand(new DebugCommand());
    }
}
