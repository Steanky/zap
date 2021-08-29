package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.*;
import io.github.zap.arenaapi.pathfind.util.Direction;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class ProxyBlockCollisionProviderTest {
    private WorldBridge worldBridge;
    private World world;
    private ProxyBlockCollisionProvider provider;

    private final Map<Vector2I, CollisionChunkView> mockChunkViews = new HashMap<>();

    private static final BoundingBox agentBounds = new BoundingBox(0, 0, 0, 1, 2, 1);
    private static final List<BoundingBox> FULL_BLOCK = new ArrayList<>();

    static {
        FULL_BLOCK.add(new BoundingBox(0, 0, 0, 1, 1, 1));
    }

    private void assertNoModification(BoundingBox bounds, Consumer<BoundingBox> consumer) {
        BoundingBox reference = bounds.clone();
        consumer.accept(bounds);
        Assertions.assertEquals(reference, bounds);
    }

    private CollisionChunkView mockChunkAt(int x, int z) {
        Vector2I location = Vectors.of(x, z);
        if(!mockChunkViews.containsKey(location)) {
            Chunk mockChunk = Mockito.mock(Chunk.class);
            CollisionChunkView mockChunkView = Mockito.mock(CollisionChunkView.class);
            mockChunkViews.put(location, mockChunkView);

            Mockito.when(worldBridge.getChunkIfLoadedImmediately(world, x, z)).thenReturn(mockChunk);
            Mockito.when(worldBridge.proxyView(mockChunk)).thenReturn(mockChunkView);

            Mockito.when(mockChunkView.position()).thenReturn(Vectors.of(x, z));
            return mockChunkView;
        }

        return mockChunkViews.get(location);
    }

    private BlockCollisionView mockBlockAt(int x, int y, int z, List<BoundingBox> voxelShapes) {
        CollisionChunkView mockChunkView = mockChunkAt(x >> 4, z >> 4);

        VoxelShapeWrapper mockVoxelShapeWrapper = Mockito.mock(VoxelShapeWrapper.class);
        Mockito.when(mockVoxelShapeWrapper.boundingBoxes()).thenReturn(voxelShapes);
        Mockito.when(mockVoxelShapeWrapper.anyBoundsMatches(ArgumentMatchers.any())).thenAnswer(invocation -> {
            BoxPredicate predicate = invocation.getArgument(0);
            for(BoundingBox bounds : voxelShapes) {
                if(predicate.test(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                        bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ())) {
                    return true;
                }
            }

            return false;
        });

        BlockCollisionView mockBlockView = Mockito.mock(BlockCollisionView.class);
        Mockito.when(mockBlockView.collision()).thenReturn(mockVoxelShapeWrapper);

        Mockito.when(mockBlockView.x()).thenReturn(x);
        Mockito.when(mockBlockView.y()).thenReturn(y);
        Mockito.when(mockBlockView.z()).thenReturn(z);

        Mockito.when(mockChunkView.collisionView(x & 15, y, z & 15)).thenReturn(mockBlockView);
        return mockBlockView;
    }

    private void testWalkDirection(List<BlockCollisionView> collisions, Direction direction, Vector3I origin) {
        CollisionChunkView chunk = mockChunkAt(origin.x() >> 4, origin.z() >> 4);

        Mockito.when(chunk.collisionsWith(ArgumentMatchers.any())).thenReturn(collisions).thenThrow();
        Assertions.assertTrue(provider.collidesMovingAlong(agentBounds, direction, Vectors.asDouble(direction)));
    }

    private BlockCollisionView[] createFullTestBlocks() {
        BlockCollisionView[] blocks = new BlockCollisionView[4];
        blocks[0] = mockBlockAt(0, 0, -1, FULL_BLOCK);
        blocks[1] = mockBlockAt(1, 0, 0, FULL_BLOCK);
        blocks[2] = mockBlockAt(0, 0, 1, FULL_BLOCK);
        blocks[3] = mockBlockAt(-1, 0, 0, FULL_BLOCK);
        return blocks;
    }

    @BeforeEach
    void setUp() {
        worldBridge = Mockito.mock(WorldBridge.class);
        world = Mockito.mock(World.class);
        provider = new ProxyBlockCollisionProvider(worldBridge, world, 1);
    }

    @Test
    void ensureCollidesMovingAlongNoModification() {
        assertNoModification(agentBounds, (bounds) -> provider.collidesMovingAlong(bounds, Direction.NORTH,
                Vectors.asDouble(Direction.NORTH)));
    }

    @Test
    void testCardinalCollisionWithFullBlocks() {
        BlockCollisionView[] blocks = createFullTestBlocks();

        testWalkDirection(List.of(blocks[0]), Direction.NORTH, Vectors.ZERO);
        testWalkDirection(List.of(blocks[1]), Direction.EAST, Vectors.ZERO);
        testWalkDirection(List.of(blocks[2]), Direction.SOUTH, Vectors.ZERO);
        testWalkDirection(List.of(blocks[3]), Direction.WEST, Vectors.ZERO);
    }

    @Test
    void testIntercardinalCollisionWithFullBlocks() {
        BlockCollisionView[] blocks = createFullTestBlocks();

        testWalkDirection(List.of(blocks[0], blocks[1]), Direction.NORTHEAST, Vectors.ZERO);
        testWalkDirection(List.of(blocks[1], blocks[2]), Direction.SOUTHEAST, Vectors.ZERO);
        testWalkDirection(List.of(blocks[2], blocks[3]), Direction.SOUTHWEST, Vectors.ZERO);
        testWalkDirection(List.of(blocks[3], blocks[0]), Direction.NORTHWEST, Vectors.ZERO);
    }
}