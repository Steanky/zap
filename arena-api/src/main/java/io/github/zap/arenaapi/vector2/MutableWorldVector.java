package io.github.zap.arenaapi.vector2;

import java.util.Objects;

public abstract class MutableWorldVector extends WorldVector {
    abstract void setWorldX(double x);

    abstract void setWorldY(double y);

    abstract void setWorldZ(double z);

    @Override
    public int hashCode() {
        return Objects.hash(worldX(), worldY(), worldZ());
    }
}
