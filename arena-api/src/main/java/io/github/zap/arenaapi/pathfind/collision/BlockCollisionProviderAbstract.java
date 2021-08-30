package io.github.zap.arenaapi.pathfind.collision;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.util.Direction;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import io.github.zap.vector.Bounds;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    protected final World world;
    protected final Map<Vector2I, CollisionChunkView> chunkViewMap;

    private final boolean supportsAsync;

    BlockCollisionProviderAbstract(@NotNull World world, @NotNull Map<Vector2I, CollisionChunkView> chunkViewMap, boolean supportsAsync) {
        this.world = world;
        this.chunkViewMap = chunkViewMap;
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
    public void updateRegion(@NotNull ChunkBounds coordinates) {}

    @Override
    public void clearRegion(@NotNull ChunkBounds coordinates) {}

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
    public boolean collidesAt(@NotNull BoundingBox worldRelativeBounds) {
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
    public @NotNull List<BlockCollisionView> collidingSolidsAt(@NotNull BoundingBox worldRelativeBounds) {
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

    @Override
    public boolean collidesMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Direction direction,
                                       @NotNull Vector3D translation) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(Vectors.asBukkit(translation));

        double width = agentBounds.getWidthX();
        double halfWidth = width / 2;
        int dirFac = direction.x() * direction.z();
        double adjustedWidth = width * ((Math.abs(direction.x()) + Math.abs(direction.z())) / 2D);

        for(BlockCollisionView shape : collidingSolidsAt(expandedBounds)) {
            VoxelShapeWrapper collision = shape.collision();

            //translate to a coordinate space centered on our entity
            double x = shape.x() - agentBounds.getCenterX();
            double y = shape.y() - agentBounds.getMinY();
            double z = shape.z() - agentBounds.getCenterZ();

            boolean collides = false;
            for(int i = 0; i < collision.size(); i++) {
                Bounds bounds = collision.boundsAt(i);

                double minX = x + bounds.minX();
                double minY = y + bounds.minY();
                double minZ = z + bounds.minZ();

                double maxX = x + bounds.maxX();
                double maxY = y + bounds.maxY();
                double maxZ = z + bounds.maxZ();

                if(collidesAtEntity(minX, minY, minZ, maxX, maxY, maxZ, halfWidth, agentBounds.getHeight())) {
                    collides = false; //skip evaluating collision for block we're in
                    break;
                }
                else if(!collides) {
                    if(direction == Direction.UP) {
                        collides = collidesAtEntity(minX, minY, minZ, maxX, maxY, maxZ, halfWidth, expandedBounds.getHeight());
                    }
                    else {
                        collides = switch (dirFac) {
                            case -1, 0 -> collisionCheck(adjustedWidth, direction.x(), direction.z(), minX, minZ, maxX, maxZ);
                            case 1 -> collisionCheck(adjustedWidth, direction.x(), direction.z(), maxX, minZ, minX, maxZ);
                            default -> throw new IllegalArgumentException("dirFac was " + dirFac);
                        };
                    }
                }
            }

            if(collides) {
                return true;
            }
        }

        return false;
    }

    private boolean collisionCheck(double width, int dirX, int dirZ, double minX, double minZ, double maxX, double maxZ) {
        /*
        inequalities:
        (y-z) < w
        (y-z) > -w
         */

        double zMinusXMin = (minZ * dirX) - (minX * dirZ);
        if(zMinusXMin >= width) { //min not in first
            return (maxZ * dirX) - (maxX * dirZ) < width; //return max in first
        }

        if(zMinusXMin > -width) { //min in first && min in second
            return true;
        }

        return (maxZ * dirX) - (maxX * dirZ) > -width; //return max in second
    }

    private boolean collidesAtEntity(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                     double halfWidth, double height) {
        return DoubleMath.fuzzyCompare(-halfWidth, maxX, Vectors.EPSILON) < 0 &&
                DoubleMath.fuzzyCompare(halfWidth, minX, Vectors.EPSILON) > 0 &&
                DoubleMath.fuzzyCompare(0, maxY, Vectors.EPSILON) < 0 &&
                DoubleMath.fuzzyCompare(height, minY, Vectors.EPSILON) > 0 &&
                DoubleMath.fuzzyCompare(-halfWidth, maxZ, Vectors.EPSILON) < 0 &&
                DoubleMath.fuzzyCompare(halfWidth, minZ, Vectors.EPSILON) > 0;
    }
}
