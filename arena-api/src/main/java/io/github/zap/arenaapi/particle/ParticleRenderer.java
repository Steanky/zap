package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ParticleRenderer {
    private final World in;
    private final Player target;
    private final List<Player> viewers;
    private final int renderInterval;

    private final Map<String, Renderable> renderables = new HashMap<>();
    private int taskId = -1;

    public ParticleRenderer(World in, Player target, int renderInterval) {
        this.in = in;
        this.target = target;
        this.viewers = null;
        this.renderInterval = renderInterval;
    }

    public ParticleRenderer(World in, int renderInterval) {
        this.in = in;
        target = null;
        viewers = null;
        this.renderInterval = renderInterval;
    }

    private void drawAll() {
        List<Player> players = viewers == null ? in.getPlayers() : viewers;

        for(Renderable renderable : renderables.values()) {
            renderable.draw(in, target, players);
        }
    }

    /**
     * Updates vectors for all Renderables. If you need to update a specific Renderable, use getRenderable and call
     * update() on it.
     */
    public void updateAll() {
        for(Renderable renderable : renderables.values()) {
            renderable.update();
        }
    }

    /**
     * Starts the renderer.
     */
    public void start() {
        if(taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), this::drawAll, 0,
                    renderInterval);
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

    public void addRenderable(Renderable renderable) {
        renderables.put(renderable.getName(), renderable);
    }

    public Renderable getRenderable(String name) {
        return renderables.get(name);
    }
}
