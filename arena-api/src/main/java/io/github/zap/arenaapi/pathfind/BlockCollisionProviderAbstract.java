package io.github.zap.arenaapi.pathfind;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    protected final World world;

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

        if(!direction.isIntercardinal()) {
            return collidesAt(expandedBounds);
        }
        else {
            double width = agentBounds.getWidthX();
            double halfWidth = width / 2;
            int dirFac = direction.x() * direction.z();
            List<BlockCollisionView> shapes = collidingSolidsAt(expandedBounds);

            for(BlockCollisionView shape : shapes) {
                VoxelShapeWrapper collision = shape.collision();

                //translate to a coordinate space centered on our entity
                double x = shape.x() - agentBounds.getCenterX();
                double z = shape.z() - agentBounds.getCenterZ();

                if(collision.anyBoundsMatches((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    minX += x;
                    minZ += z;

                    maxX += x;
                    maxZ += z;

                    if(DoubleMath.fuzzyCompare(minX, halfWidth, Vectors.EPSILON) < 0 &&
                            DoubleMath.fuzzyCompare(maxX, -halfWidth, Vectors.EPSILON) > 0 &&
                            DoubleMath.fuzzyCompare(minZ, halfWidth, Vectors.EPSILON) < 0 &&
                            DoubleMath.fuzzyCompare(maxZ, -halfWidth, Vectors.EPSILON) > 0) {
                        return false;
                    }
                    else {
                        return switch (dirFac) {
                            case 1 -> diagonalCollisionCheck(width, dirFac, maxX, minZ, minX, maxZ);
                            case -1 -> diagonalCollisionCheck(width, dirFac, minX, minZ, maxX, maxZ);
                            default -> throw new IllegalArgumentException("dirFac was " + dirFac);
                        };

                    }
                })) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean diagonalCollisionCheck(double width, int dirFac, double minX, double minZ, double maxX, double maxZ) {
        double zMinusXMin = minZ - (minX * dirFac);
        if(zMinusXMin >= width) { //min not in first
            return maxZ - (maxX * dirFac) < width; //return max in first
        }

        if(zMinusXMin > -width) { //min in first && min in second
            return true;
        }

        return maxZ - (maxX * dirFac) > -width; //return max in second
    }
}
