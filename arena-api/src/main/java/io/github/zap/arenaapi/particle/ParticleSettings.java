package io.github.zap.arenaapi.particle;

import lombok.Value;
import org.bukkit.Particle;

@Value
public class ParticleSettings {
    Particle particle;
    int count;
    double offsetX;
    double offsetY;
    double offsetZ;
    double extra;
    Object data;

    public ParticleSettings(Particle particle, int count) {
        this.particle = particle;
        this.count = count;
        offsetX = 0;
        offsetY = 0;
        offsetZ = 0;
        extra = 0;
        data = null;
    }
}
