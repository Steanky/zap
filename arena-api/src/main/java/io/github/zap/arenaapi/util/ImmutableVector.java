package io.github.zap.arenaapi.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.util.Vector;

/**
 * Immutable vector implementation. It can be faster than Bukkit vectors in some circumstances due to the fact that it
 * can cache the results of certain calculations (and because it is immutable).
 */
@RequiredArgsConstructor
public class ImmutableVector {
    public final double x;
    public final double y;
    public final double z;

    private boolean hasMagnitudeSquared = false;
    private double magnitudeSquared;

    private boolean hasMagnitude = false;
    private double magnitude;

    public ImmutableVector(Vector of) {
        this.x = of.getBlockX();
        this.y = of.getBlockY();
        this.z = of.getBlockZ();
    }

    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }

    public double magnitude() {
        getMagnitude();
        return magnitude;
    }

    public double magnitudeSquared() {
        getSquaredValues();
        return magnitudeSquared;
    }

    public double distance(double x, double y, double z) {
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) + Math.pow(this.z - z, 2));
    }

    public double distance(ImmutableVector to) {
        return distance(to.x, to.y, to.z);
    }

    public ImmutableVector add(double x, double y, double z) {
        return new ImmutableVector(this.x + x, this.y + y,this.z + z);
    }

    public ImmutableVector add(double value) {
        return add(value, value, value);
    }

    public ImmutableVector add(ImmutableVector to) {
        return add(to.x, to.y, to.z);
    }

    public ImmutableVector subtract(double x, double y, double z) {
        return new ImmutableVector(this.x - x, this.y - y,this.z - z);
    }

    public ImmutableVector subtract(double value) {
        return subtract(value, value, value);
    }

    public ImmutableVector subtract(ImmutableVector other) {
        return subtract(other.x, other.y, other.z);
    }

    public ImmutableVector multiply(double x, double y, double z) {
        return new ImmutableVector(this.x * x, this.y * y,this.z * z);
    }

    public ImmutableVector multiply(double value) {
        return multiply(value, value, value);
    }

    public ImmutableVector multiply(ImmutableVector other) {
        return multiply(other.x, other.y, other.z);
    }

    public ImmutableVector divide(double x, double y, double z) {
        return new ImmutableVector(this.x / x, this.y / y,this.z / z);
    }

    public ImmutableVector divide(double value) {
        return divide(value, value, value);
    }

    public ImmutableVector divide(ImmutableVector other) {
        return divide(other.x, other.y, other.z);
    }

    private void getSquaredValues() {
        if(!hasMagnitudeSquared) {
            magnitudeSquared = x*x + y*y + z*z;
            hasMagnitudeSquared = true;
        }
    }

    private void getMagnitude() {
        if(!hasMagnitude) {
            getSquaredValues();
            magnitude = Math.sqrt(magnitudeSquared);
            hasMagnitude = true;
        }
    }
}
