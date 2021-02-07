package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Simple function that takes a positional vector (in world coordinates) and converts it to FragmentData.
 */
public interface Shader {
    FragmentData generateFragment(Vector position);
}
