package io.github.zap.arenaapi.particle;

import lombok.RequiredArgsConstructor;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class SolidShader implements Shader {
    private final Particle particle;
    private final int count;
    private final Object data;

    @Override
    public FragmentData generateFragment(Vector position) {
        return new FragmentData(position.getX(), position.getY(), position.getZ(), particle, count, data);
    }
}
