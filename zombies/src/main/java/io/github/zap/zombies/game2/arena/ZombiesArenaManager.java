package io.github.zap.zombies.game2.arena;

import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.arenaapi.stats.StatsManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ZombiesArenaManager extends ArenaManager<ZombiesArena> {

    public final static String GAME_NAME = "zombies";

    public ZombiesArenaManager(@NotNull Location hubLocation, @NotNull StatsManager statsManager) {
        super(GAME_NAME, hubLocation, statsManager);
    }

    @Override
    public void handleJoin(JoinInformation joinInformation, Consumer<Pair<Boolean, String>> consumer) {

    }

    @Override
    public boolean acceptsPlayers() {
        return false;
    }

    @Override
    public void unloadArena(ZombiesArena zombiesArena) {

    }

    @Override
    public boolean hasMap(String s) {
        return false;
    }

}
