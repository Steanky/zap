package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * This class periodically runs code on the main server thread, at a specified rate.
 */
public class Ticker {
    private final List<Tickable> tickables = new ArrayList<>();
    private final Set<String> pendingRemoval = new HashSet<>();
    private int taskId = -1;

    @Getter
    private int tps;

    /**
     * Registers a Tickable instance, whose onTick() method will be called once per the period specified on this
     * Ticker's creation.
     * @param tickable The Tickable to register
     */
    public void register(Tickable tickable) {
        tickables.add(tickable);
    }

    /**
     * Scheduels the specified Tickable for removal (its onTick() method will no longer be called afterwards). The
     * actual removal will likely not happen immediately.
     * @param tickable The Tickable to remove
     */
    public void remove(Tickable tickable) {
        if(tickables.contains(tickable)) {
            pendingRemoval.add(tickable.getName());
        }
    }

    /**
     * Starts this Ticker instance with the specified ticks per second (TPS).
     * @param tps The ticks per second (TPS) this Tickable should run at
     */
    public void start(int tps) {
        Validate.isTrue(tps > 0 && tps <= 20, "tps must be in the range 0 < tps <= 20");

        if(taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombiesPlugin.getInstance(), this::doTick, 0,
                    (20/tps) - 1);
            this.tps = tps;
        }
    }

    /**
     * Stops the Ticker instance, which allows it to be started with a different TPS and prevents it from calling
     * any Tickables. Redundantly calling this method has no effect.
     */
    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            tps = -1;
        }
    }

    private void doTick() {
        for(int i = tickables.size() - 1; i > -1; i--) { //reverse iteration because we may delete values
            Tickable tickable = tickables.get(i);
            String name = tickable.getName();

            if(pendingRemoval.contains(name)) { //this list is empty 99% of the time and contains is 0(1)
                tickables.remove(i);
                pendingRemoval.remove(name);
            }
            else {
                tickable.onTick();
            }
        }
    }
}