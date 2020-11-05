package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import org.bukkit.Bukkit;

import java.util.*;

public class Ticker {
    private final List<Tickable> tickables = new ArrayList<>();
    private final Set<String> pendingRemoval = new HashSet<>();
    private int taskId = -1;

    private void doTick() {
        for(int i = tickables.size() - 1; i > -1; i--) {
            Tickable tickable = tickables.get(i);
            String name = tickable.getName();

            if(pendingRemoval.contains(name)) {
                tickables.remove(i);
                pendingRemoval.remove(name);
            }
            else {
                tickable.doTick();
            }
        }
    }

    public void register(Tickable tickable) {
        tickables.add(tickable);
    }

    public void remove(Tickable tickable) {
        pendingRemoval.add(tickable.getName());
    }

    public void remove(String name) {
        pendingRemoval.add(name);
    }

    public void start(long period) {
        if(taskId == -1){
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombiesPlugin.getInstance(), this::doTick, 0,
                    period);
        }
    }

    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}