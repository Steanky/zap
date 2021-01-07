package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ParticleRenderer implements Disposable {
    @Getter
    private final World world;

    @Getter
    private final int tickInterval;

    private final List<RenderComponent> components = new ArrayList<>();

    private int renderTask = -1;

    public ParticleRenderer(World world, int tickInterval) {
        this.world = world;
        this.tickInterval = tickInterval;
    }

    private void startIfAny() {
        if(renderTask == -1 && components.size() > 0) {
            renderTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), () -> {
                for(RenderComponent component : components) { //iterate all components
                    ParticleSettings settings = component.particleData();

                    for(Vector vector : component.getFragments()) { //spawn each particle
                        world.spawnParticle(settings.getParticle(), component.renderTo(), null, vector.getX(),
                                vector.getY(), vector.getZ(), settings.getCount(), settings.getOffsetX(),
                                settings.getOffsetY(), settings.getOffsetZ(), settings.getExtra(), settings.getData(),
                                settings.isForce());
                    }
                }
            }, 0, tickInterval);
        }
    }

    private void stopIfEmpty() {
        if(renderTask != -1 && components.size() == 0) {
            Bukkit.getScheduler().cancelTask(renderTask);
            renderTask = -1;
        }
    }

    public void addComponent(RenderComponent component) {
        components.add(component);
        startIfAny();
    }

    public void removeComponent(RenderComponent component) {
        components.remove(component);
        stopIfEmpty();
    }

    public void removeComponentAt(int index) {
        components.remove(index);
        stopIfEmpty();
    }

    public int componentCount() {
        return components.size();
    }

    public void clearComponents() {
        components.clear();
        stopIfEmpty();
    }

    @Override
    public void dispose() {
        clearComponents();
    }
}
