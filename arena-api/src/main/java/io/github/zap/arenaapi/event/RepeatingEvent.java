package io.github.zap.arenaapi.event;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Event that fires periodically; based on Bukkit's scheduler. It is created in a non-started state and must be started
 * with start() before it begins firing events.
 */
public class RepeatingEvent extends Event<EmptyEventArgs> {
    private final Plugin plugin;
    private final int delay;
    private final int period;

    private int taskId = -1;

    public RepeatingEvent(@NotNull Plugin plugin, int delay, int period) {
        this.plugin = plugin;
        this.delay = delay;
        this.period = period;
    }

    /**
     * Starts the RepeatingEvent, which will call its handlers on the main server thread indefinitely (until it is
     * stopped via a call to stop())
     */
    public void start() {
        if(taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> callEvent(EmptyEventArgs
                    .getInstance()), delay, period);
        }
    }

    /**
     * Stops the RepeatingEvent.
     */
    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Performs cleanup tasks.
     */
    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        super.dispose();
        stop();
    }
}
