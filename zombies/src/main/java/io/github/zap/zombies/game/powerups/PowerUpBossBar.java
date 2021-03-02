package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;

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
        formatter = new DecimalFormat("##.##");
        arena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        arena.getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
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
        var items = arena.getPowerUps().stream()
                .filter(x -> x instanceof DurationPowerUp);

        StringBuilder sb = new StringBuilder();
        sb.append(longest.getData().getDisplayName());
        items.limit(3).forEach(x -> sb.append(ChatColor.GRAY + ", ").append(x.getData().getDisplayName()));
        if(items.count() > 3) {
            sb.append(ChatColor.DARK_GRAY + "...");
        }
        var ticks = (longest.getEstimatedEndTimeStamp() - System.currentTimeMillis());
        sb.append(" " + ChatColor.GRAY + " - ").append(formatter.format(ticks / 20f)).append("seconds");

        if(items.anyMatch(x -> true)) {
            if(!bukkitBossBar.isVisible()) bukkitBossBar.setVisible(true);
            bukkitBossBar.setTitle(sb.toString());
            bukkitBossBar.setColor(((DurationPowerUpData)longest.getData()).getBossBarColor());
            bukkitBossBar.setProgress(ticks / (float)((DurationPowerUpData)longest.getData()).getDuration());
        } else {
            bukkitBossBar.setVisible(false);
        }
    }

    private DurationPowerUp findLongest() {
        DurationPowerUp longest = null;

        for(var item : arena.getPowerUps()) {
            var current = (DurationPowerUp)item;
            if(longest == null || current.getEstimatedEndTimeStamp() > longest.getEstimatedEndTimeStamp()) {
                longest = current;
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
