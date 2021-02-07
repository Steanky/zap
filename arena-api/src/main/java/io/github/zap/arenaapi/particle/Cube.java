package io.github.zap.arenaapi.particle;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Cube implements VectorProvider {
    private static final Vector UNIT = new Vector(1, 1, 1);

    private final BoundingBox bounds;
    private final int density;

    private final Line[] lines = new Line[12];
    private final int[] lengths = new int[12];

    private int length = -1;

    private int i = 0;
    private int j = 0;

    public Cube(BoundingBox bounds, int density) {
        this.bounds = bounds;
        this.density = density;
    }

    @Override
    public int init() {
        if(length == -1) {
            Vector origin = bounds.getMin();
            Vector limit = bounds.getMax().add(UNIT);

            Vector one = new Vector(origin.getX(), origin.getY(), limit.getZ());
            Vector two = new Vector(limit.getX(), origin.getY(), limit.getZ());
            Vector three = new Vector(limit.getX(), origin.getY(), origin.getZ());
            Vector four = new Vector(origin.getX(), limit.getY(), origin.getZ());
            Vector five = new Vector(origin.getX(), limit.getY(), limit.getZ());
            Vector seven = new Vector(limit.getX(), limit.getY(), origin.getZ());

            lines[0] = new Line(origin, one, density);
            lines[1] = new Line(origin, two, density);
            lines[2] = new Line(two, three, density);
            lines[3] = new Line(three, origin, density);

            lines[4] = new Line(origin, four, density);
            lines[5] = new Line(one, five, density);
            lines[6] = new Line(two, limit, density);
            lines[7] = new Line(three, seven, density);

            lines[8] = new Line(four, five, density);
            lines[9] = new Line(five, limit, density);
            lines[10] = new Line(limit, seven, density);
            lines[11] = new Line(seven, four, density);

            length = 0;

            for(int i = 0; i < 12; i++) {
                int lineLength = lines[i].init();
                lengths[i] = lineLength;
                length += lineLength;
            }
        }

        return length;
    }

    @Override
    public Vector next() {
        Line current = lines[i];
        Vector vector = current.next();

        if(++j == lengths[i]) {
            i++;
            j = 0;

            current.reset();
        }

        return vector;
    }

    @Override
    public void reset() {
        i = 0;
        j = 0;
    }
}
