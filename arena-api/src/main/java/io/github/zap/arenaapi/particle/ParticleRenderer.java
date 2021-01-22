package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleRenderer implements Disposable {
    private final World in;
    private final Player sender;
    private final List<Player> receivers;
    private final int renderInterval;

    private final Map<String, Renderable> renderables = new HashMap<>();
    private int taskId = -1;

    /**
     * Creates a new ParticleRenderer that broadcasts particles in the specified World, from the specified sender,
     * visible to the specified receivers, at the supplied interval.
     * @param in The world the particles should appear in
     * @param sender The player that is sending the particles. The sender will NOT see the particles unless they are
     *               also present in the receivers list. This parameter is used as a check to avoid redundantly
     *               sending particle packets: each receiving player will perform a line of sight check on the sender
     *               and only those that pass will actually see the particles.
     *
     *               This can be set to null in order to disable LoS checks.
     * @param receivers The receiving players; those who will see the particles assuming they have a line-of-sight to
     *                  the sending Player.
     *
     *                  This can be set to null in order to broadcast the particles to all players in the world.
     * @param renderInterval The interval at which to spawn particles.
     */
    public ParticleRenderer(World in, Player sender, List<Player> receivers, int renderInterval) {
        this.in = in;
        this.sender = sender;
        this.receivers = receivers;
        this.renderInterval = renderInterval;
    }

    public ParticleRenderer(World in, Player sender, int renderInterval) {
        this(in, sender, null, renderInterval);
    }

    public ParticleRenderer(World in, int renderInterval) {
        this(in, null, null, renderInterval);
    }

    private void drawAll() {
        for(Renderable renderable : renderables.values()) {
            renderable.draw(in, sender, receivers);
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

    public void addRenderable(RenderableProvider provider) {
        Renderable renderable = provider.getRenderable();
        renderables.put(renderable.getName(), renderable);
    }

    public Renderable getRenderable(String name) {
        return renderables.get(name);
    }

    public Renderable removeRenderable(String name) {
        return renderables.remove(name);
    }

    @Override
    public void dispose() {
        stop();
    }
}
