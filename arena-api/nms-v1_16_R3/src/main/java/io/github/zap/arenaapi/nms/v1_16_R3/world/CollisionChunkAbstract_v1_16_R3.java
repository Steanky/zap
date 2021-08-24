package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import net.minecraft.server.v1_16_R3.Chunk;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class CollisionChunkAbstract_v1_16_R3 implements CollisionChunkView {
    private class SnapshotIterator implements Iterator<BlockCollisionView> {
        private final int startX;
        private final int startY;

        private final int endX;
        private final int endY;
        private final int endZ;

        private int x;
        private int y;
        private int z;

        SnapshotIterator(@NotNull BoundingBox overlap) {
            Vector min = overlap.getMin();
            Vector max = overlap.getMax();

            startX = min.getBlockX();
            startY = min.getBlockY();

            x = startX - 1;
            y = startY;
            z = min.getBlockZ();

            endX = max.getBlockX() + 1;
            endY = max.getBlockY() + 1;
            endZ = max.getBlockZ() + 1;
        }

        @Override
        public boolean hasNext() {
            int nextX = x + 1;
            int nextY = y;
            int nextZ = z;

            if(nextX == endX) {
                nextY++;
            }

            if(nextY == endY) {
                nextZ++;
            }

            return nextZ < endZ;
        }

        @Override
        public BlockCollisionView next() {
            if(++x == endX) {
                if(++y == endY) {
                    z++;
                    y = startY;
                }

                x = startX;
            }

            int chunkX = x & 15;
            int chunkY = y;
            int chunkZ = z & 15;

            assertValidChunkCoordinate(chunkX, chunkY, chunkZ);
            return makeSnapshot(chunkX, chunkY, chunkZ);
        }
    }

    protected final int x;
    protected final int z;

    protected final int originX;
    protected final int originZ;

    private final BoundingBox chunkBounds;

    CollisionChunkAbstract_v1_16_R3(@NotNull Chunk chunk) {
        this.x = chunk.locX;
        this.z = chunk.locZ;

        this.originX = x << 4;
        this.originZ = z << 4;

        this.chunkBounds = new BoundingBox(originX, 0, originZ, originX + 16, 255, originZ + 16);
    }

    @Override
    public @Nullable BlockCollisionView collisionView(int chunkX, int chunkY, int chunkZ) {
        assertValidChunkCoordinate(chunkX, chunkY, chunkZ);
        return makeSnapshot(chunkX, chunkY, chunkZ);
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds) {
        if(chunkBounds.overlaps(worldRelativeBounds)) {
            BoundingBox overlap = worldRelativeBounds.clone().intersection(chunkBounds);
            SnapshotIterator iterator = new SnapshotIterator(overlap);

            while(iterator.hasNext()) {
                BlockCollisionView snapshot = iterator.next();

                if(snapshot != null && snapshot.overlaps(overlap)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockCollisionView> collisionsWith(@NotNull BoundingBox worldBounds) {
        List<BlockCollisionView> shapes = new ArrayList<>();

        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.clone().intersection(chunkBounds);
            SnapshotIterator iterator = new SnapshotIterator(overlap);

            while(iterator.hasNext()) {
                BlockCollisionView snapshot = iterator.next();

                if(snapshot != null && snapshot.overlaps(overlap)) {
                    shapes.add(snapshot);
                }
            }
        }

        return shapes;
    }

    protected void assertValidChunkCoordinate(int x, int y, int z) {
        if(x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
            throw new IllegalArgumentException("Invalid chunk coordinates [" + x + ", " + y + ", " + z + "]");
        }
    }

    protected abstract BlockCollisionView makeSnapshot(int chunkX, int chunkY, int chunkZ);
}