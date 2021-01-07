package io.github.zap.arenaapi.util;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class VectorUtils {
    public static double manhattanDistance(Vector first, Vector second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) +
                Math.abs(first.getZ() - second.getZ());
    }

    public static Vector[] interpolateLine(Vector s, Vector e, int density) {
        double distance = s.distance(e);

        Vector start = s.clone();
        Vector end = e.clone();

        end.add(start.clone().multiply(-1)); //end is now a vector with the same angle as the vector formed from s to e

        int particles = (int)Math.round(distance * density);
        Vector[] vectors = new Vector[particles];
        end.multiply(1D / particles); //end is now a vector with a length equaling the distance between particles

        for(int i = 0; i < particles; i++) {
            vectors[i] = start.clone();
            start.add(end);
        }

        return vectors;
    }

    public static Vector[] particleAabb(BoundingBox box, int density) {
        Vector origin = box.getMin();
        Vector limit = box.getMax();

        //ignore the unintuitive numbering; i created these while looking at a poorly-constructed visual aid
        Vector one = new Vector(origin.getX(), origin.getY(), limit.getZ());
        Vector two = new Vector(limit.getX(), origin.getY(), limit.getZ());
        Vector three = new Vector(limit.getX(), origin.getY(), origin.getZ());
        Vector four = new Vector(origin.getX(), limit.getY(), origin.getZ());
        Vector five = new Vector(origin.getX(), limit.getY(), limit.getZ());
        Vector seven = new Vector(limit.getX(), limit.getY(), origin.getZ());

        Vector[] lineOne = interpolateLine(origin, one, density);
        Vector[] lineTwo = interpolateLine(one, two, density);
        Vector[] lineThree = interpolateLine(two, three, density);
        Vector[] lineFour = interpolateLine(three, origin, density);

        Vector[] lineFive = interpolateLine(origin, four, density);
        Vector[] lineSix = interpolateLine(one, five, density);
        Vector[] lineSeven = interpolateLine(two, limit, density);
        Vector[] lineEight = interpolateLine(three, seven, density);

        Vector[] lineNine = interpolateLine(four, five, density);
        Vector[] lineTen = interpolateLine(five, limit, density);
        Vector[] lineEleven = interpolateLine(limit, seven, density);
        Vector[] lineTwelve = interpolateLine(seven, four, density);

        return ArrayUtils.combine(lineOne, lineTwo, lineThree, lineFour, lineFive, lineSix, lineSeven, lineEight,
                lineNine, lineTen, lineEleven, lineTwelve);
    }
}
