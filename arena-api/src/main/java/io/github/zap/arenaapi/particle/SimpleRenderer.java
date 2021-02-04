package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SimpleRenderer implements Renderer {
    private final World world;
    private final int initialDelay;
    private final int period;

    private final List<Renderable> renderables = new ArrayList<>();
    private int taskId = -1;

    @Override
    public void start() {
        if(taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), this::draw, initialDelay,
                    period);
        }
    }

    @Override
    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @Override
    public void draw() {
        for(Renderable renderable : renderables) {
            for(FragmentData fragment : renderable.getFragments()) {
                world.spawnParticle(fragment.getParticle(), fragment.getX(), fragment.getY(), fragment.getZ(),
                        fragment.getCount(), fragment.getData());
            }
        }
    }

    @Override
    public void add(Renderable renderable) {
        renderables.add(renderable);
    }

    @Override
    public void remove(int index) {
        renderables.remove(index);
    }
}
