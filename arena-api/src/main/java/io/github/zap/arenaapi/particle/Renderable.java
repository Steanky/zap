package io.github.zap.arenaapi.particle;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Renderable {
    private boolean shouldBake = false;
    private RenderComponent[] baked = null;

    private final Map<String, RenderComponent> renderComponents = new HashMap<>();

    /**
     * Renders each of the RenderComponents in this Renderable object.
     */
    public void draw(World in, Player to, List<Player> players) {
        if(shouldBake) {
            //it is faster to iterate an array and worth converting when necessary seeing as we run this code very often
            baked = renderComponents.values().toArray(new RenderComponent[0]);
            shouldBake = false;
        }

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
        String name = component.getName();

        if(renderComponents.putIfAbsent(name, component) == null) {
            shouldBake = true;
        }
    }
}
