package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Simple function that takes a positional vector and converts it to FragmentData. Vectors are always provided in
 * world (global) coordinate space.
 */
@FunctionalInterface
public interface Shader {
    FragmentData generateFragment(Vector position);
}
