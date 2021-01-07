package io.github.zap.arenaapi.particle;

import lombok.Getter;
import org.bukkit.Particle;

/**
 * Encapsulates the arguments that may be passed to spawnParticle, with overloads for convenience.
 */
@Getter
public class ParticleSettings {
    private final Particle particle;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double extra;
    private final Object data;
    private final boolean force;

    public ParticleSettings(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra, Object data, boolean force) {
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
        this.data = data;
        this.force = force;
    }

    public ParticleSettings(Particle particle, int count, Object data) {
        this(particle, count, 0, 0, 0, 0, data, false);
    }

    public ParticleSettings(Particle particle, int count) {
        this(particle, count, 0, 0, 0, 0, null, false);
    }

    public ParticleSettings(Particle particle) {
        this(particle, 1, 0, 0, 0, 0, null, false);
    }
}
