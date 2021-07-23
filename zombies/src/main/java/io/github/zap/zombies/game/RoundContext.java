package io.github.zap.zombies.game;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RoundContext(@NotNull List<@NotNull BukkitTask> spawnTasks,
                           @NotNull List<@NotNull BukkitTask> removeTasks,
                           @NotNull List<@NotNull ActiveMob> spawnedMobs) {

    public void reset() {
        for (BukkitTask task : spawnTasks) {
            task.cancel();
        }

        for (BukkitTask task : removeTasks) {
            task.cancel();
        }

        for (ActiveMob mob : spawnedMobs) {
            Entity entity = mob.getEntity().getBukkitEntity();

            if (entity != null) {
                entity.remove();
            }
        }
    }

}
