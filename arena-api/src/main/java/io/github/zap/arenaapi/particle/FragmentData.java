package io.github.zap.arenaapi.particle;

import lombok.Value;
import org.bukkit.Particle;

@Value
public class FragmentData {
    double x;
    double y;
    double z;
    Particle particle;
    int count;
    Object data;
}
