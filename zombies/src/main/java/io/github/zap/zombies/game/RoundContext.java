package io.github.zap.zombies.game;

import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public record RoundContext(List<BukkitTask> spawnTasks, List<BukkitTask> removeTasks) {
    void cancelTasks() {
        for(BukkitTask task : spawnTasks) {
            task.cancel();
        }

        for(BukkitTask task : removeTasks) {
            task.cancel();
        }
    }
}
