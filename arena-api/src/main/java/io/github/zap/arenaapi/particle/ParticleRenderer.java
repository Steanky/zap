package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ParticleRenderer {
    private final World in;
    private final Player target;
    private final List<Player> viewers;
    private final int renderInterval;

    private final List<Renderable> renderables = new ArrayList<>();
    private int taskId = -1;

    private void drawAll() {
        for(Renderable renderable : renderables) {
            renderable.draw(in, target, viewers);
        }
    }

    /**
     * Starts the renderer.
     */
    public void start() {
        if(taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), this::drawAll,
                    0, renderInterval);
        }
    }

    /**
     * Stops the renderer.
     */
    public void stop() {
        if(taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
