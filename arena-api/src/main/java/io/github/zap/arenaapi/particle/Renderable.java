package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.Disposable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

@RequiredArgsConstructor
public class Renderable {
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
     * Updates a specific RenderComponent.
     * @param component The name of the RenderComponent to update.
     */
    public void update(String component) {
        RenderComponent renderComponent = renderComponents.get(component);

        if(renderComponent != null) {
            renderComponent.updateVectors();
        }
    }

    /**
     * Renders all visual components of this Renderable object, given the specified parameters.
     * @param in The world that the particles should spawn in
     * @param sender The sending player. May be null, which will cause the particles to not perform LoS checks and
     *               send particles to all receiving players
     * @param receivers The players receiving the particles. May be null, which will cause all players in World <i>in</i>
     *                  to receive the particles
     */
    public void draw(World in, Player sender, List<Player> receivers) {
        tryBakeAll();

        for(RenderComponent component : baked) {
            ParticleSettings settings = component.getSettings();
            Vector[] vectors = component.getVectors();

            if(vectors != null) {
                for(Vector vector : vectors) {
                    in.spawnParticle(settings.getParticle(), receivers, sender, vector.getX(), vector.getY(), vector.getZ(),
                            settings.getCount(), settings.getOffsetX(), settings.getOffsetY(), settings.getOffsetZ(),
                            settings.getExtra(), settings.getData(), settings.isForce());
                }
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

    public List<String> getComponentNames() {
        return new ArrayList<>(renderComponents.keySet());
    }
}
