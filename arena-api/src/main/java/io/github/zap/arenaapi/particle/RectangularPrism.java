package io.github.zap.arenaapi.particle;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class RectangularPrism extends CompositeProvider {
    public RectangularPrism(BoundingBox bounds, double density) {
        super(12);

        Vector origin = bounds.getMin();
        Vector limit = bounds.getMax();

        Vector one = new Vector(origin.getX(), origin.getY(), limit.getZ());
        Vector two = new Vector(limit.getX(), origin.getY(), limit.getZ());
        Vector three = new Vector(limit.getX(), origin.getY(), origin.getZ());
        Vector four = new Vector(origin.getX(), limit.getY(), origin.getZ());
        Vector five = new Vector(origin.getX(), limit.getY(), limit.getZ());
        Vector seven = new Vector(limit.getX(), limit.getY(), origin.getZ());

        providers[0] = new Line(origin, one, density);
        providers[1] = new Line(one, two, density);
        providers[2] = new Line(two, three, density);
        providers[3] = new Line(three, origin, density);

        providers[4] = new Line(origin, four, density);
        providers[5] = new Line(one, five, density);
        providers[6] = new Line(two, limit, density);
        providers[7] = new Line(three, seven, density);

        providers[8] = new Line(four, five, density);
        providers[9] = new Line(five, limit, density);
        providers[10] = new Line(limit, seven, density);
        providers[11] = new Line(seven, four, density);
    }
}
