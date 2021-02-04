package io.github.zap.arenaapi.particle;

import lombok.Value;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@Value
public class FragmentData {
    double x;
    double y;
    double z;
    Particle particle;
    int count;
    Object data;

    //TODO: this is slow, make it not slow
    public static FragmentData[] of(Vector[] vectors, Particle particle, int count, Object data) {
        FragmentData[] frags = new FragmentData[vectors.length];

        for(int i = 0; i < vectors.length; i++)
        {
            Vector vector = vectors[i];
            frags[i] = new FragmentData(vector.getX(), vector.getY(), vector.getZ(), particle, count, data);
        }

        return frags;
    }
}
