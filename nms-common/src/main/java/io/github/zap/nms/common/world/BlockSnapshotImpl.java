package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

class BlockSnapshotImpl implements BlockSnapshot {
    private final VectorAccess worldVector;
    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockSnapshotImpl(@NotNull VectorAccess worldVector, @NotNull BlockData data,
                      @NotNull VoxelShapeWrapper collision) {
        this.worldVector = worldVector;
        this.data = data;
        this.collision = collision;
    }

    @Override
    public @NotNull VectorAccess position() {
        return worldVector;
    }

    @Override
    public @NotNull BlockData data() {
        return data;
    }

    @Override
    public @NotNull VoxelShapeWrapper collision() {
        return collision;
    }

    @Override
    public boolean overlaps(@NotNull BoundingBox worldBounds) { //TODO: Optimize this (currently it works but is slow zz low iq)
        AtomicBoolean collided = new AtomicBoolean();

        //will likely need to add a better method to VoxelShapeWrapper
        collision.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            if(new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ).shift(worldVector.asBukkit()).overlaps(worldBounds)) {
                collided.set(true);
            }
        });

        return collided.get();
    }
}
