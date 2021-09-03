package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.vector.*;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    @FunctionalInterface
    private interface ViewPredicate {
        boolean test(BlockCollisionView block, Vector3D shapeVector);
    }

    protected final World world;
    protected final Map<Long, CollisionChunkView> chunkViewMap;

    private final boolean supportsAsync;

    BlockCollisionProviderAbstract(@NotNull World world, @NotNull Map<Long, CollisionChunkView> chunkViewMap,
                                   boolean supportsAsync) {
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
    public @NotNull List<BlockCollisionView> solidsOverlapping(@NotNull BoundingBox worldRelativeBounds) {
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
    public @NotNull HitResult collisionMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Direction direction,
                                                   @NotNull Vector3D translation) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(Vectors.asBukkit(translation)).expand(-Vectors.EPSILON);

        double width = agentBounds.getWidthX();
        double halfWidth = width / 2;

        List<BlockCollisionView> collisionViews = solidsOverlapping(expandedBounds);
        removeCollidingAtAgent(agentBounds, collisionViews);

        Direction opposite = direction.opposite();

        if(direction == Direction.UP || direction == Direction.DOWN || direction.isCardinal()) {
            return nearestView(collisionViews, agentBounds, opposite, (shape, shapeVector) -> true);
        }
        else if(direction.isIntercardinal()) {
            Direction first = direction.rotateClockwise();
            Direction second = first.opposite();

            double adjustedWidth = (width * (Math.abs(direction.x()) + Math.abs(direction.z()))) / (2);

            return nearestView(collisionViews, agentBounds, opposite, (shape, shapeVector) -> {
                VoxelShapeWrapper collision = shape.collision();
                Vector3D firstPoint = Vectors.add(collision.positionAtSide(first), shapeVector);
                Vector3D secondPoint = Vectors.add(collision.positionAtSide(second), shapeVector);

                return collisionCheck(adjustedWidth, direction.x(), direction.z(), firstPoint.x() - halfWidth,
                        firstPoint.z() - halfWidth, secondPoint.x() - halfWidth, secondPoint.z() - halfWidth);
            });
        }
        else {
            throw new IllegalArgumentException("Direction " + direction + " not supported");
        }
    }

    protected long chunkKey(int x, int z) {
        return ((long) x << 32) | z;
    }

    private HitResult nearestView(Iterable<BlockCollisionView> collisions, BoundingBox agentBounds, Direction opposite,
                                  ViewPredicate filter) {
        double nearestMagnitudeSquared = Double.POSITIVE_INFINITY;
        BlockCollisionView nearestCollision = null;
        boolean collides = false;

        for(BlockCollisionView shape : collisions) {
            Vector3D shapeVector = Vectors.of(shape.x() - agentBounds.getMinX(), shape.y() - agentBounds.getMinY(),
                    shape.z() - agentBounds.getMinZ());

            if(filter.test(shape, shapeVector)) {
                VoxelShapeWrapper collision = shape.collision();

                Vector3D nearestVector = Vectors.add(collision.positionAtSide(opposite), shapeVector);
                double currentMagnitudeSquared = Vectors.distanceSquared(nearestVector.x(), nearestVector.y(),
                        nearestVector.z(), 0.5, 0, 0.5);

                if(currentMagnitudeSquared < nearestMagnitudeSquared) {
                    nearestCollision = shape;
                    nearestMagnitudeSquared = currentMagnitudeSquared;
                    collides = true;
                }
            }
        }

        return new HitResult(collides, nearestCollision, nearestMagnitudeSquared);
    }

    private void removeCollidingAtAgent(BoundingBox agentBounds, List<BlockCollisionView> hits) {
        for(int i = hits.size() - 1; i >= 0; i--) {
            if(hits.get(i).overlaps(agentBounds)) {
                hits.remove(i);
            }
        }
    }

    private boolean collisionCheck(double adjustedWidth, int dirX, int dirZ, double minX, double minZ, double maxX, double maxZ) {
        /*
        inequalities:
        (y-z) < w
        (y-z) > -w
         */

        double zMinusXMin = (minZ * dirX) - (minX * dirZ);
        if(zMinusXMin >= adjustedWidth) { //min not in first
            return (maxZ * dirX) - (maxX * dirZ) < adjustedWidth; //return max in first
        }

        if(zMinusXMin > -adjustedWidth) { //min in first && min in second
            return true;
        }

        return (maxZ * dirX) - (maxX * dirZ) > -adjustedWidth; //return max in second
    }
}
