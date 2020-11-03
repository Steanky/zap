package io.github.zap.game;

import io.github.zap.ZombiesPlugin;
import org.bukkit.Bukkit;

import java.util.*;

public class Ticker {
    private final List<Tickable> tickables = new ArrayList<>();
    private final Set<String> remove = new HashSet<>();
    private int taskId = -1;

    private void doTick() {
        for(int i = tickables.size() - 1; i > -1; i--) {
            Tickable tickable = tickables.get(i);
            String name = tickable.getName();

            if(remove.contains(name)) {
                tickables.remove(i);
                remove.remove(name);
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
        remove.add(tickable.getName());
    }

    public void start(long period) {
        if(taskId == -1){
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombiesPlugin.getInstance(), this::doTick, 0, period);
        }
    }

    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
