package io.github.zap.zombies.game2.arena.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface HiddenPlayersManager {

    void addPlayer(@NotNull Supplier<Player> player);

    void removePlayer(@NotNull Player player);

    void showHiddenPlayersToPlayer(@NotNull Player player);

    void hideHiddenPlayersForPlayer(@NotNull Player player);

}
