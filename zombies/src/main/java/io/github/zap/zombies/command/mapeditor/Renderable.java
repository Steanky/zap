package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.particle.RenderComponent;

import java.util.List;

/**
 * Interfaced implemented by MapData objects. Represents an object capable of providing renderable visual
 * representations of its data.
 */
public interface Renderable {
    /**
     * Returns and calculates the RenderComponent objects for this class.
     * @return A list of RenderComponent objects that are used to visually display this object
     */
    List<RenderComponent> getRenderComponents();
}
