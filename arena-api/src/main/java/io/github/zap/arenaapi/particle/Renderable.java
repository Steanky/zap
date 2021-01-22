package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.Disposable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class Renderable {
    private static final RenderComponent[] EMPTY_RENDER_COMPONENT_ARRAY = new RenderComponent[0];

    private boolean shouldBake = false;
    private RenderComponent[] baked = null;

    private final Map<String, RenderComponent> renderComponents = new HashMap<>();

    @Getter
    private final String name;

    /**
     * This exists because iterating an array is ~35% faster than map.values() and we iterate through RenderComponents
     * a lot
     */
    private void tryBakeAll() {
        if(shouldBake) {
            baked = renderComponents.values().toArray(EMPTY_RENDER_COMPONENT_ARRAY);
            shouldBake = false;
        }
    }

    /**
     * Updates all component vectors for this Renderable.
     */
    public void update() {
        tryBakeAll();

        for(RenderComponent component : baked) {
            component.updateVectors();
        }
    }

    /**
     * Renders each of the RenderComponents in this Renderable object.
     */
    public void draw(World in, Player to, List<Player> players) {
        tryBakeAll();

        for(RenderComponent component : baked) {
            ParticleSettings settings = component.getSettings();

            for(Vector vector : component.getVectors()) {
                in.spawnParticle(settings.getParticle(), players, to, vector.getX(), vector.getY(), vector.getZ(),
                        settings.getCount(), settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ(),
                        settings.getExtra(), settings.getData(), settings.isForce());
            }
        }
    }

    public RenderComponent getComponent(String name) {
        return renderComponents.get(name);
    }

    public void addComponent(RenderComponent component) {
        renderComponents.put(component.getName(), component);
        shouldBake = true;
    }

    public void addComponents(Collection<RenderComponent> components) {
        for(RenderComponent component : components) {
            renderComponents.put(component.getName(), component);
        }

        shouldBake = true;
    }
}
