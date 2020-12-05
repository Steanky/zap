package io.github.zap.arenaapi;

import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ArenaApi extends JavaPlugin {
    @Getter
    private static ArenaApi instance;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        timer.stop();
        getLogger().info(String.format("Done enabling; ~%sms elapsed", timer.getTime()));
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