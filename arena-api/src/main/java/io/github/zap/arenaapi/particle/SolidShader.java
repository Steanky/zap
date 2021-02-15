package io.github.zap.arenaapi.particle;

import lombok.RequiredArgsConstructor;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Shader implementation that always gives the same particle, count, and data regardless of position.
 */
@RequiredArgsConstructor
public class SolidShader implements Shader {
    private final Particle particle;
    private final int count;
    private final Object data;

    @Override
    public FragmentData generateFragment(Vector position) {
        return new FragmentData(particle, position.getX(), position.getY(), position.getZ(), count, 0, 0,
                0, 0, data, true);
    }
}
