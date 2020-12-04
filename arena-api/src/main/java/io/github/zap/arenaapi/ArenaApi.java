package io.github.zap.arenaapi;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.playerdata.FileDataManager;
import io.github.zap.arenaapi.playerdata.FilePlayerData;
import io.github.zap.arenaapi.playerdata.PlayerDataManager;
import io.github.zap.arenaapi.serialize.BukkitDataLoader;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.ValueConverter;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public final class ArenaApi extends JavaPlugin {
    @Getter
    private static ArenaApi instance;

    @Getter
    private DataLoader dataLoader;

    @Getter
    private LocalizationManager localizationManager;

    @Getter
    private PlayerDataManager playerDataManager;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            localizationManager = new LocalizationManager(Locale.US, new File("localization"));
            dataLoader = new BukkitDataLoader(FilePlayerData.class);
            playerDataManager = new FileDataManager(new File("playerdata.yml"), 4096);

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

            timer.stop();
            getLogger().info(String.format("Done enabling; ~%sms elapsed", timer.getTime()));
        }
        catch (LoadFailureException exception) {
            getLogger().severe(String.format("A fatal error occured that prevented the plugin from enabling properly:" +
                    " '%s'", exception.getMessage()));
            getPluginLoader().disablePlugin(this, false);
        }
    }

    @Override
    public void onDisable() {
        StopWatch timer = StopWatch.createStarted();

        List<ArenaManager<?>> arenas = new ArrayList<>(arenaManagers.values());
        for(int i = arenas.size() - 1; i >= 0; i--) {
            arenas.remove(i).terminate();
        }

        getLogger().info("Terminated ArenaManagers...");

        timer.stop();
        getLogger().info(String.format("Done disabling; ~%sms elapsed", timer.getTime()));
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
}