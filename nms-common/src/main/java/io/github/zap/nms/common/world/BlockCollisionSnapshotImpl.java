package io.github.zap.nms.common.world;

import io.github.zap.vector.VectorAccess;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

class BlockCollisionSnapshotImpl implements BlockCollisionSnapshot {
    private final VectorAccess chunkVector;
    private final BlockData data;
    private final VoxelShapeWrapper collision;

    BlockCollisionSnapshotImpl(@NotNull VectorAccess chunkVector, @NotNull BlockData data,
                               @Nullable VoxelShapeWrapper collision) {
        this.chunkVector = chunkVector;
        this.data = data;
        this.collision = collision;
    }

    @Override
    public @NotNull VectorAccess position() {
        return chunkVector;
    }

    @Override
    public @NotNull BlockData data() {
        return data;
    }

    @Override
    public @Nullable VoxelShapeWrapper collision() {
        return collision;
    }

    @Override
    public boolean overlaps(@NotNull BoundingBox chunkRelativeBounds) {
        if(collision == null) {
            return false;
        }

        BoundingBox blockRelative = toBlockRelative(chunkRelativeBounds);

        AtomicBoolean collided = new AtomicBoolean();
        collision.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            if(blockRelative.overlaps(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ))) {
                collided.set(true);
            }
        });

        return collided.get();
    }

    @Override
    public double height() {
        return 0;
    }

    private BoundingBox toBlockRelative(BoundingBox chunkRelative) {
        return chunkRelative.shift(chunkVector.asBukkit().multiply(-1));
    }
}
