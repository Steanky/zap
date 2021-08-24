package io.github.zap.zombies.game2.player;

import io.github.zap.arenaapi.hotbar2.HotbarManager;
import io.github.zap.arenaapi.hotbar2.PlayerView;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game2.player.armor.ArmorHolder;
import io.github.zap.zombies.game2.player.coin.Coins;
import io.github.zap.zombies.game2.player.kills.Kills;
import io.github.zap.zombies.game2.player.task.PlayerTask;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ZombiesPlayer implements Damager {

    private final PlayerView playerView;

    private final ArmorHolder armorHolder;

    private final HotbarManager hotbarManager;

    private final Kills kills;

    private final Coins coins;

    private final List<PlayerTask> tasks;

    private String state = ZombiesPlayerState.ALIVE;

    private boolean inGame = true;

    public ZombiesPlayer(@NotNull PlayerView playerView, @NotNull ArmorHolder armorHolder,
                         @NotNull HotbarManager hotbarManager, @NotNull Kills kills, @NotNull Coins coins,
                         @NotNull List<PlayerTask> tasks) {
        this.playerView = playerView;
        this.armorHolder = armorHolder;
        this.hotbarManager = hotbarManager;
        this.kills = kills;
        this.coins = coins;
        this.tasks = tasks;
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull ArmorHolder getArmorHolder() {
        return armorHolder;
    }

    public @NotNull HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public @NotNull Kills getKills() {
        return kills;
    }

    public @NotNull Coins getCoins() {
        return coins;
    }

    public @NotNull String getState() {
        return state;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void tick() {
        for (PlayerTask task : tasks) {
            task.tick();
        }
    }

    @Override
    public void onDealsDamage(@NotNull DamageAttempt item, @NotNull Mob damaged, double deltaHealth) {

    }

}
