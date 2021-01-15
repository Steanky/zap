package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParticleRenderer implements Disposable {
    @Getter
    private final World world;

    @Getter
    private final int tickInterval;

    private final Map<String, RenderComponent> components = new HashMap<>();
    private final Player player;
    private final List<Player> targets;

    private int renderTask = -1;

    public ParticleRenderer(World world, int tickInterval, List<Player> targets, Player player) {
        this.world = world;
        this.tickInterval = tickInterval;
        this.player = player;
        this.targets = targets;
    }

    public ParticleRenderer(World world, int tickInterval, Player sender) {
        this(world, tickInterval, null, sender);
    }

    public ParticleRenderer(World world, int tickInterval) {
        this(world, tickInterval, null, null);
    }

    private void startIfAny() {
        if(renderTask == -1 && components.size() > 0) {
            renderTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(ArenaApi.getInstance(), () -> {
                for(RenderComponent component : components.values()) { //iterate all components
                    ParticleSettings settings = component.particleData();

                    for(Vector vector : component.getFragments()) { //spawn each particle
                        world.spawnParticle(settings.getParticle(), targets != null ? targets : world.getPlayers(),
                                player, vector.getX(), vector.getY(), vector.getZ(), settings.getCount(),
                                settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ(), settings.getExtra(),
                                settings.getData(), settings.isForce());
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
        components.put(component.name(), component);
        startIfAny();
    }

    public RenderComponent removeComponent(String name) {
        RenderComponent component = components.remove(name);
        stopIfEmpty();
        return component;
    }

    public void addComponents(Collection<RenderComponent> components) {
        for(RenderComponent component : components) {
            this.components.put(component.name(), component);
        }
    }

    public void removeAllMatching(Predicate<RenderComponent> componentPredicate) {
        components.entrySet().removeIf((entry) -> componentPredicate.test(entry.getValue()));
    }

    public int componentCount() {
        return components.size();
    }

    public RenderComponent computeIfAbsent(String name, Function<? super String, ? extends RenderComponent> mapper) {
        RenderComponent renderComponent = components.computeIfAbsent(name, mapper);
        startIfAny();
        return renderComponent;
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
