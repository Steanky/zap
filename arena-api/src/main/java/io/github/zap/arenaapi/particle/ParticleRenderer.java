package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.util.WorldUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ParticleRenderer implements Disposable {
    @Getter
    private final World world;

    @Getter
    private final Player target;

    @Getter
    private final boolean isLocal;

    @Getter
    private final int tickInterval;

    private final List<RenderComponent> components = new ArrayList<>();

    private int renderTask = -1;

    public ParticleRenderer(Player target, int tickInterval) {
        this.world = target.getWorld();
        this.target = target;
        this.isLocal = true;
        this.tickInterval = tickInterval;
    }

    public ParticleRenderer(World world, int tickInterval) {
        this.world = world;
        this.target = null;
        this.isLocal = false;
        this.tickInterval = tickInterval;
    }

    private void start() {
        if(renderTask == -1 && components.size() > 0) {
            renderTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), () -> {
                for(RenderComponent component : components) {
                    ParticleSettings settings = component.particleData();

                    for(Vector vector : component.getFragments()) {
                        if(isLocal) {
                            target.spawnParticle(settings.getParticle(), vector.getX(), vector.getY(), vector.getZ(),
                                    settings.getCount(), settings.getOffsetX(), settings.getOffsetY(),
                                    settings.getOffsetZ(), settings.getExtra(), settings.getData());
                        }
                        else {
                            world.spawnParticle(settings.getParticle(), vector.getX(), vector.getY(), vector.getZ(),
                                    settings.getCount(), settings.getOffsetX(), settings.getOffsetY(),
                                    settings.getOffsetZ(), settings.getExtra(), settings.getData());
                        }
                    }
                }
            }, 0, tickInterval);
        }
    }

    private void stop() {
        if(renderTask != -1 && components.size() == 0) {
            Bukkit.getScheduler().cancelTask(renderTask);
            renderTask = -1;
        }
    }

    public void addComponent(RenderComponent component) {
        components.add(component);
        start();
    }

    public void removeComponent(RenderComponent component) {
        components.remove(component);
        stop();
    }

    public void clearComponents() {
        components.clear();
        stop();
    }

    @Override
    public void dispose() {
        clearComponents();
    }
}
