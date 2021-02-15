package io.github.zap.arenaapi.particle;

import lombok.Value;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@Value
public class FragmentData {
    static Vector UNIT = new Vector(1, 1, 1);

    Particle particle;
    double x;
    double y;
    double z;
    int count;
    double offsetX;
    double offsetY;
    double offsetZ;
    double extra;
    Object data;
    boolean force;
}
