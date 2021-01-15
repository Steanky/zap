package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Represents an object that is used by the particle renderer.
 */
public interface RenderComponent {
    RenderComponent[] EMPTY_RENDER_COMPONENT_ARRAY = new RenderComponent[0];

    /**
     * Gets all the vectors for this RenderComponent.
     * @return An array of vectors to render
     */
    Vector[] getFragments();

    /**
     * Updates the fragment array for this render component.
     */
    void updateFragments();

    /**
     * Get the particle used for this RenderComponent.
     * @return The particle used for this component
     */
    ParticleSettings particleData();

    /**
     * The name of this component, used by the ParticleRenderer to uniquely identify it.
     * @return The name of this component
     */
    String name();
}
