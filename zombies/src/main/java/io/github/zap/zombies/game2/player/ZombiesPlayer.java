package io.github.zap.zombies.game2.player;

import io.github.zap.arenaapi.hotbar2.HotbarManager;
import io.github.zap.arenaapi.hotbar2.PlayerView;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game2.player.coin.CoinManager;
import io.github.zap.zombies.game2.player.kills.KillManager;
import io.github.zap.zombies.game2.player.state.PlayerStateManager;
import io.github.zap.zombies.game2.player.task.PlayerTask;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ZombiesPlayer implements Damager {

    private final PlayerView playerView;

    private final PlayerStateManager playerStateManager;

    private final HotbarManager hotbarManager;

    private final KillManager killManager;

    private final CoinManager coinManager;

    private final List<PlayerTask> tasks;

    private boolean inGame = true;

    public ZombiesPlayer(@NotNull PlayerView playerView, @NotNull PlayerStateManager playerStateManager,
                         @NotNull HotbarManager hotbarManager, @NotNull KillManager killManager,
                         @NotNull CoinManager coinManager, @NotNull List<PlayerTask> tasks) {
        this.playerView = playerView;
        this.playerStateManager = playerStateManager;
        this.hotbarManager = hotbarManager;
        this.killManager = killManager;
        this.coinManager = coinManager;
        this.tasks = tasks;
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public @NotNull HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public @NotNull CoinManager getCoinManager() {
        return coinManager;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void tickTasks() {
        for (PlayerTask task : tasks) {
            task.tick();
        }
    }

    @Override
    public void onDealsDamage(@NotNull DamageAttempt item, @NotNull Mob damaged, double deltaHealth) {

    }

}
