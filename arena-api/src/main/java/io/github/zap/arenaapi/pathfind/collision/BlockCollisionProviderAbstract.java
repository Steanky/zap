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

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
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

        List<BlockCollisionView> collisionViews = solidsOverlapping(expandedBounds);
        removeCollidingAtAgent(agentBounds, collisionViews);

        BlockCollisionView nearestCollision = null;
        double nearestMagnitudeSquared = Double.POSITIVE_INFINITY;
        boolean anyCollides = false;

        Direction opposite = direction.opposite();

        if(direction == Direction.UP || direction == Direction.DOWN || direction.isCardinal()) {
            for(BlockCollisionView shape : collisionViews) {
                Vector3D shapeVector = Vectors.of(shape.x() - agentBounds.getCenterX(),
                        shape.y() - agentBounds.getMinY(), shape.z() - agentBounds.getCenterZ());

                VoxelShapeWrapper collision = shape.collision();

                Vector3D nearestVector = Vectors.add(collision.positionAtSide(opposite), shapeVector);
                double currentMagnitudeSquared = Vectors.magnitudeSquared(nearestVector);

                if(currentMagnitudeSquared < nearestMagnitudeSquared) {
                    nearestCollision = shape;
                    nearestMagnitudeSquared = currentMagnitudeSquared;
                }

                anyCollides = true;
            }
        }
        else if(direction.isIntercardinal()) {
            Direction first = direction.rotateClockwise();
            Direction second = first.opposite();

            double adjustedWidth = (width * (Math.abs(direction.x()) + Math.abs(direction.z()))) / (2);
            for(BlockCollisionView shape : collisionViews) {
                Vector3D shapeVector = Vectors.of(shape.x() - agentBounds.getCenterX(),
                        shape.y() - agentBounds.getMinY(), shape.z() - agentBounds.getCenterZ());

                VoxelShapeWrapper collision = shape.collision();
                Vector3D firstPoint = Vectors.add(collision.positionAtSide(first), shapeVector);
                Vector3D secondPoint = Vectors.add(collision.positionAtSide(second), shapeVector);

                if(collisionCheck(adjustedWidth, direction.x(), direction.z(), firstPoint.x(), firstPoint.z(),
                        secondPoint.x(), secondPoint.z())) {
                    Vector3D nearestVector = Vectors.add(collision.positionAtSide(opposite), shapeVector);
                    double currentMagnitudeSquared = Vectors.magnitudeSquared(nearestVector);

                    if(currentMagnitudeSquared < nearestMagnitudeSquared) {
                        nearestCollision = shape;
                        nearestMagnitudeSquared = currentMagnitudeSquared;
                    }

                    anyCollides = true;
                }
            }
        }
        else {
            throw new IllegalArgumentException("Direction " + direction + " not supported");
        }

        return new HitResult(anyCollides, nearestCollision, nearestMagnitudeSquared);
    }

    protected long chunkKey(int x, int z) {
        return ((long) x << 32) | z;
    }

    private void removeCollidingAtAgent(BoundingBox agentBounds, List<BlockCollisionView> hits) {
        Iterator<BlockCollisionView> iterator = hits.listIterator();
        if(iterator.hasNext()) {
            BlockCollisionView sample = iterator.next();
            if(sample.overlaps(agentBounds)) {
                iterator.remove();
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
