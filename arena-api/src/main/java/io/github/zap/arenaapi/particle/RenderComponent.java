package io.github.zap.arenaapi.particle;

import org.bukkit.Particle;
import org.bukkit.util.Vector;

public interface RenderComponent {
    /**
     * Gets all the vectors for this RenderComponent.
     * @return An array of vectors to render
     */
    Vector[] getFragments();

    /**
     * Get the particle used for this RenderComponent.
     * @return The particle used for this component
     */
    Particle getParticle();

    /**
     * Gets the number of particles spawned for this component.
     * @return The number of particles spawned for this component
     */
    int getParticleDensity();
}
