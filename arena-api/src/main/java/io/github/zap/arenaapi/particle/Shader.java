package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

public interface Shader {
    FragmentData[] generateFragments(Vector[] positions);
}
