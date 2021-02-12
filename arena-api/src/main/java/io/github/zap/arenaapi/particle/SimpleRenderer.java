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

    private Renderable[] baked;
    private final List<Renderable> renderables = new ArrayList<>();
    private int taskId = -1;

    private boolean shouldBake;
    private static final Renderable[] EMPTY_RENDERABLE_ARRAY = new Renderable[0];

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
        tryBake();

        for(Renderable renderable : baked) {
            for(FragmentData fragment : renderable.getFragments()) {
                world.spawnParticle(fragment.getParticle(), fragment.getX(), fragment.getY(), fragment.getZ(),
                        fragment.getCount(), fragment.getData());
            }
        }
    }

    @Override
    public void add(Renderable renderable) {
        renderables.add(renderable);
        shouldBake = true;
    }

    @Override
    public void remove(int index) {
        renderables.remove(index);
        shouldBake = true;
    }

    @Override
    public void set(int index, Renderable value) {
        renderables.set(index, value);
        shouldBake = true;
    }

    @Override
    public Renderable get(int index) {
        return renderables.get(index);
    }

    @Override
    public int size() {
        return renderables.size();
    }

    private void tryBake() {
        if(shouldBake) {
            baked = renderables.toArray(EMPTY_RENDERABLE_ARRAY);
            shouldBake = false;
        }
    }
}
