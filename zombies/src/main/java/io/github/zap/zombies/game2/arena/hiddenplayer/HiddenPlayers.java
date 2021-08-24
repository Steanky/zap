package io.github.zap.zombies.game2.arena.hiddenplayer;

import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HiddenPlayers {

    void addPlayer(@NotNull PlayerView playerView);

    void removePlayer(@NotNull Player player);

    void showHiddenPlayersToPlayer(@NotNull Player player);

    void hideHiddenPlayersForPlayer(@NotNull Player player);

    void unhideAll();

}
