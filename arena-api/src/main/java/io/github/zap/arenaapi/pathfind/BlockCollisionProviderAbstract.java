package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.vector.Vector2I;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    private final World world;
    private final boolean supportsAsync;

    BlockCollisionProviderAbstract(@NotNull World world, boolean supportsAsync) {
        this.world = world;
        this.supportsAsync = supportsAsync;
    }

    @Override
    public @NotNull World world() {
        return world;
    }

    @Override
    public boolean supportsAsync() {
        return supportsAsync;
    }

    @Override
    public void updateRegion(@NotNull ChunkCoordinateProvider coordinates) {}

    @Override
    public void clearRegion(@NotNull ChunkCoordinateProvider coordinates) {}

    @Override
    public void clearForWorld() {}

    @Override
    public @Nullable BlockCollisionView getBlock(int x, int y, int z) {
        CollisionChunkView view = chunkAt(x >> 4, z >> 4);

        if(view != null) {
            return view.collisionView(x & 15, y, z & 15);
        }

        return null;
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds) {
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            Vector2I chunkCoordinates = iterator.next();
            CollisionChunkView chunk = chunkAt(chunkCoordinates.x(), chunkCoordinates.z());

            if(chunk != null && chunk.collidesWithAny(worldRelativeBounds)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockCollisionView> collidingSolids(@NotNull BoundingBox worldRelativeBounds) {
        List<BlockCollisionView> shapes = new ArrayList<>();
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            Vector2I chunkCoordinates = iterator.next();
            CollisionChunkView chunk = chunkAt(chunkCoordinates.x(), chunkCoordinates.z());

            if(chunk != null) {
                shapes.addAll(chunk.collisionsWith(worldRelativeBounds));
            }
        }

        return shapes;
    }
}
