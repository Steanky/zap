package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.util.ArrayUtils;
import lombok.Value;
import org.bukkit.Particle;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Value
public class FragmentData {
    static Vector UNIT = new Vector(1, 1, 1);

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

    public static FragmentData[] interpolateLine(Vector start, Vector end, int density, Particle particle, int count,
                                                 Object data) {
        double distance = start.distance(end);

        Vector startClone = start.clone();
        Vector endClone = end.clone();

        endClone.add(startClone.clone().multiply(-1)); //end is now a vector with the same angle as the vector formed from s to e

        int particles = (int)Math.round(distance * density);
        FragmentData[] fragments = new FragmentData[particles];
        endClone.multiply(1D / particles); //end is now a vector with a length equaling the distance between particles

        for(int i = 0; i < particles; i++) {
            Vector pos = startClone.clone();
            fragments[i] = new FragmentData(pos.getX(), pos.getY(), pos.getZ(), particle, count, data);
            startClone.add(endClone);
        }

        return fragments;
    }
    
    public static FragmentData[] interpolateBounds(BoundingBox box, int density, Particle particle, int count,
                                                   Object data) {
        Vector origin = box.getMin();
        Vector limit = box.getMax().add(UNIT);

        Vector one = new Vector(origin.getX(), origin.getY(), limit.getZ());
        Vector two = new Vector(limit.getX(), origin.getY(), limit.getZ());
        Vector three = new Vector(limit.getX(), origin.getY(), origin.getZ());
        Vector four = new Vector(origin.getX(), limit.getY(), origin.getZ());
        Vector five = new Vector(origin.getX(), limit.getY(), limit.getZ());
        Vector seven = new Vector(limit.getX(), limit.getY(), origin.getZ());

        return ArrayUtils.combine(interpolateLine(origin, one, density, particle, count, data), interpolateLine(one, two, density, particle, count, data),
                interpolateLine(two, three, density, particle, count, data), interpolateLine(three, origin, density, particle, count, data),
                interpolateLine(origin, four, density, particle, count, data), interpolateLine(one, five, density, particle, count, data),
                interpolateLine(two, limit, density, particle, count, data), interpolateLine(three, seven, density, particle, count, data),
                interpolateLine(four, five, density, particle, count, data), interpolateLine(five, limit, density, particle, count, data),
                interpolateLine(limit, seven, density, particle, count, data), interpolateLine(seven, four, density, particle, count, data));
    }

    public static FragmentData[] interpolateBounds(MultiBoundingBox box, int density, Particle particle, int count,
                                             Object data) {
        List<FragmentData[]> fragments = new ArrayList<>(box.getBounds().size());

        for(BoundingBox bounds : box.getBounds()) {
            FragmentData[] vectors = interpolateBounds(bounds, density, particle, count, data);
            fragments.add(vectors);
        }

        return ArrayUtils.combine(fragments, FragmentData.class);
    }
}
