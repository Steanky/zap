package io.github.zap.arenaapi;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Manages {@link BukkitTask}s by cancelling them all upon disposal
 */
public class BukkitTaskManager extends ResourceManager {

    private final Plugin plugin;

    public BukkitTaskManager(@NotNull Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Schedules a task for the next tick
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTask(@NotNull Runnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        DisposableBukkitRunnable bukkitRunnable = new DisposableBukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        addDisposable(bukkitRunnable);
        return bukkitRunnable.runTask(plugin);
    }

    /**
     * Schedules a task for the next tick
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTask(@NotNull DisposableBukkitRunnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        addDisposable(task);
        return task.runTask(plugin);
    }

    /**
     * Schedules a task
     * @param delay The time to schedule the task after in ticks
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTaskLater(long delay, @NotNull Runnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        DisposableBukkitRunnable bukkitRunnable = new DisposableBukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        addDisposable(bukkitRunnable);
        return bukkitRunnable.runTaskLater(plugin, delay);
    }

    /**
     * Schedules a task
     * @param delay The time to schedule the task after in ticks
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTaskLater(long delay, @NotNull DisposableBukkitRunnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        addDisposable(task);
        return task.runTaskLater(plugin, delay);
    }

    /**
     * Schedules a task to run periodically
     * @param delay The time to schedule the task after in ticks
     * @param period The period to run the task over
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTaskTimer(long delay, long period, @NotNull Runnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        DisposableBukkitRunnable bukkitRunnable = new DisposableBukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        addDisposable(bukkitRunnable);
        return bukkitRunnable.runTaskTimer(plugin, period, delay);
    }

    /**
     * Schedules a task to run periodically
     * @param delay The time to schedule the task after in ticks
     * @param period The period to run the task over
     * @param task The task to schedule
     * @return The new {@link BukkitTask}
     */
    public @NotNull BukkitTask runTaskTimer(long delay, long period, @NotNull DisposableBukkitRunnable task) {
        if (isDisposed()) {
            throw new ObjectDisposedException();
        }

        addDisposable(task);
        return task.runTaskTimer(plugin, delay, period);
    }

}
