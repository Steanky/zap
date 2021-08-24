package io.github.zap.zombies.game2.arena.player;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface PlayerList<T> {

    @NotNull Map<UUID, T> getPlayers();

}
