package io.github.zap.arenaapi.particle;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Represents an object that can be visually rendered.
 */
public interface RenderComponent {
    /**
     * Gets all the vectors for this RenderComponent.
     * @return An array of vectors to render
     */
    Vector[] getFragments();

    /**
     * Updates the fragment array for this render component.
     */
    void updateFragments(Vector[] fragments);

    /**
     * Get the particle used for this RenderComponent.
     * @return The particle used for this component
     */
    ParticleSettings particleData();

    /**
     * A list of players who will be able to see this RenderComponent.
     * @return The players who can see this object
     */
    List<Player> renderTo();

    /**
     * The name of this component, used to uniquely identify it.
     * @return The name of this component
     */
    String name();
}
