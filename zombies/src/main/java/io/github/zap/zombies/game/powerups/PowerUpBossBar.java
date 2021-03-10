package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.powerups.DurationPowerUpData;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * This class displays information about activated power ups
 */
public class PowerUpBossBar extends BukkitRunnable implements Disposable {
    final BossBar bukkitBossBar;
    final BukkitTask updateTask;
    final ZombiesArena arena;
    final DecimalFormat formatter;
    boolean isVisible;

    public PowerUpBossBar(ZombiesArena arena, int refreshRate) {
        this.arena = arena;
        bukkitBossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        updateTask = runTaskTimer(Zombies.getInstance(), 0, refreshRate);
        formatter = new DecimalFormat("##.#");
        arena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        arena.getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        bukkitBossBar.setVisible(false);
    }

    private void onPlayerLeave(ManagingArena<ZombiesArena, ZombiesPlayer>.ManagedPlayerListArgs managedPlayerListArgs) {
        managedPlayerListArgs.getPlayers().forEach(x -> bukkitBossBar.removePlayer(x.getPlayer()));
    }

    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        playerListArgs.getPlayers().forEach(bukkitBossBar::addPlayer);
    }

    @Override
    public void run() {
        var longest = findLongest();
        if(longest == null) {
            bukkitBossBar.setVisible(false);
            return;
        }


        var items = arena.getPowerUps().stream()
                .filter(x -> x instanceof DurationPowerUp && x.getState() == PowerUpState.ACTIVATED)
                .filter(x -> x != longest)
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder();
        sb.append(longest.getData().getDisplayName());
        items.stream().limit(3).forEach(x -> sb.append(ChatColor.RESET).append(ChatColor.GRAY).append(", ").append(x.getData().getDisplayName()));
        if(items.size() > 3) {
            sb.append(ChatColor.DARK_GRAY).append("...");
        }
        var millis = (longest.getEstimatedEndTimeStamp() - System.currentTimeMillis());
        sb.append(ChatColor.GRAY).append(" - ").append(formatter.format(millis / 1000f)).append("seconds");

        if(!bukkitBossBar.isVisible()) bukkitBossBar.setVisible(true);
        bukkitBossBar.setTitle(sb.toString());
        bukkitBossBar.setColor(((DurationPowerUpData)longest.getData()).getBossBarColor());
        bukkitBossBar.setProgress(millis / 50f / (float)((DurationPowerUpData)longest.getData()).getDuration());
    }

    private DurationPowerUp findLongest() {
        DurationPowerUp longest = null;

        for(var item : arena.getPowerUps()) {
            if(item instanceof DurationPowerUp && item.getState() == PowerUpState.ACTIVATED) {
                var current = (DurationPowerUp)item;
                if(longest == null || current.getEstimatedEndTimeStamp() > longest.getEstimatedEndTimeStamp()) {
                    longest = current;
                }
            }
        }
        return longest;
    }

    @Override
    public void dispose() {
        if(!updateTask.isCancelled())
            updateTask.cancel();

        arena.getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
        arena.getPlayerLeaveEvent().removeHandler(this::onPlayerLeave);
        bukkitBossBar.removeAll();
    }
}
