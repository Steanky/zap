package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.*;
import io.github.zap.vector.*;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class ProxyBlockCollisionProviderTest {
    private WorldBridge worldBridge;
    private World world;
    private ProxyBlockCollisionProvider provider;

    private final Map<Vector2I, CollisionChunkView> mockChunkViews = new HashMap<>();

    private final BoundingBox fullAgentBounds = new BoundingBox(0, 0, 0, 1, 2, 1);
    private final BoundingBox tinyAgentBounds = new BoundingBox(0.4, 0, 0.4, 0.6, 2, 0.6);

    private final List<BoundingBox> fullBlock = new ArrayList<>();
    private final List<BoundingBox> tinyBlock = new ArrayList<>();
    private final List<BoundingBox> stairs = new ArrayList<>();

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
            Mockito.when(worldBridge.proxyView(Mockito.eq(mockChunk), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong(),
                    ArgumentMatchers.any())).thenReturn(mockChunkView);

            Mockito.when(mockChunkView.position()).thenReturn(Vectors.of(x, z));
            return mockChunkView;
        }

        return mockChunkViews.get(location);
    }

    private BlockCollisionView mockBlockAt(int x, int y, int z, List<BoundingBox> voxelShapes, boolean shouldOverlap) {
        CollisionChunkView mockChunkView = mockChunkAt(x >> 4, z >> 4);

        VoxelShapeWrapper mockVoxelShapeWrapper = Mockito.mock(VoxelShapeWrapper.class);
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

        Mockito.when(mockVoxelShapeWrapper.size()).thenReturn(voxelShapes.size());

        Mockito.when(mockVoxelShapeWrapper.positionAtSide(ArgumentMatchers.any())).thenAnswer(invocation -> {
            Direction direction = ((Direction)invocation.getArgument(0)).opposite();
            if(voxelShapes == fullBlock || voxelShapes == stairs) {
                return Vectors.of((double)(direction.x() == -1 ? 0 : direction.x()), direction.y() == -1 ? 0 : direction.y(),
                        direction.z() == -1 ? 0 : direction.z());
            }
            else if(voxelShapes == tinyBlock) {
                double dX = direction.x() == -1 ? 0.4 : (direction.x() == 1 ? 0.6 : 0);
                double dY = direction.y() == -1 ? 0 : direction.y();
                double dZ = direction.z() == -1 ? 0.4 : (direction.z() == 1 ? 0.6 : 0);
                return Vectors.of(dX, dY, dZ);
            }
            else {
                throw new IllegalArgumentException("List not supported");
            }
        });

        int i = 0;
        for(BoundingBox bounds : voxelShapes) {
            Mockito.when(mockVoxelShapeWrapper.boundsAt(i++)).thenAnswer(invocation ->
                    new Bounds(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                            bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()));
        }


        BlockCollisionView mockBlockView = Mockito.mock(BlockCollisionView.class);
        Mockito.when(mockBlockView.collision()).thenReturn(mockVoxelShapeWrapper);
        Mockito.when(mockBlockView.overlaps(ArgumentMatchers.any())).thenReturn(shouldOverlap);

        Mockito.when(mockBlockView.x()).thenReturn(x);
        Mockito.when(mockBlockView.y()).thenReturn(y);
        Mockito.when(mockBlockView.z()).thenReturn(z);

        Mockito.when(mockChunkView.collisionView(x & 15, y, z & 15)).thenReturn(mockBlockView);
        return mockBlockView;
    }

    private void testWalkDirection(BoundingBox agentBounds, List<BlockCollisionView> collisions, Direction direction,
                                   Vector3I origin, boolean collides) {
        CollisionChunkView chunk = mockChunkAt(origin.x() >> 4, origin.z() >> 4);

        Mockito.when(chunk.collisionsWith(ArgumentMatchers.any())).thenReturn(collisions).thenThrow();
        BlockCollisionProvider.HitResult result = provider.collisionMovingAlong(agentBounds, direction,
                Vectors.asDouble(direction));
        Assertions.assertSame(collides, result.collides());
        System.out.println(result.nearestDistanceSquared());
    }

    private void testCardinal(BoundingBox agentBounds, BlockCollisionView[] collisions, boolean collides) {
        testWalkDirection(agentBounds, List.of(collisions[0]), Direction.NORTH, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[1]), Direction.EAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[2]), Direction.SOUTH, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[3]), Direction.WEST, Vectors.ZERO, collides);
    }

    private void testIntercardinal(BoundingBox agentBounds, BlockCollisionView[] collisions, boolean collides) {
        testWalkDirection(agentBounds, List.of(collisions[0], collisions[1]), Direction.NORTHEAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[1], collisions[2]), Direction.SOUTHEAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[2], collisions[3]), Direction.SOUTHWEST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, List.of(collisions[3], collisions[0]), Direction.NORTHWEST, Vectors.ZERO, collides);
    }

    private void testCardinalSameCollision(BoundingBox agentBounds, List<BlockCollisionView> collisions, boolean collides) {
        testWalkDirection(agentBounds, collisions, Direction.NORTH, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.EAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.SOUTH, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.WEST, Vectors.ZERO, collides);
    }

    private void testIntercardinalSameCollision(BoundingBox agentBounds, List<BlockCollisionView> collisions, boolean collides) {
        testWalkDirection(agentBounds, collisions, Direction.NORTHEAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.SOUTHEAST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.SOUTHWEST, Vectors.ZERO, collides);
        testWalkDirection(agentBounds, collisions, Direction.NORTHWEST, Vectors.ZERO, collides);
    }

    private BlockCollisionView[] createCardinallyAdjacentTestBlocks(List<BoundingBox> blockBounds) {
        BlockCollisionView[] blocks = new BlockCollisionView[4];
        blocks[0] = mockBlockAt(0, 0, -1, blockBounds, false);
        blocks[1] = mockBlockAt(1, 0, 0, blockBounds, false);
        blocks[2] = mockBlockAt(0, 0, 1, blockBounds, false);
        blocks[3] = mockBlockAt(-1, 0, 0, blockBounds, false);
        return blocks;
    }

    @BeforeEach
    void setUp() {
        worldBridge = Mockito.mock(WorldBridge.class);
        world = Mockito.mock(World.class);
        provider = new ProxyBlockCollisionProvider(worldBridge, world, 1, 0, TimeUnit.SECONDS);

        fullBlock.add(new BoundingBox(0, 0, 0, 1, 1, 1));
        tinyBlock.add(new BoundingBox(0.4, 0, 0.4, 0.6, 1, 0.6));

        stairs.add(new BoundingBox(0, 0.5, 0, 1, 1, 1));
        stairs.add(new BoundingBox(0, 0, 0, 0.5, 0.5, 1));
    }

    @Test
    void ensureCollidesMovingAlongNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.collisionMovingAlong(bounds, Direction.NORTH,
                Vectors.asDouble(Direction.NORTH)));
    }

    @Test
    void ensureCollidesAtNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.collidesAt(bounds));
    }

    @Test
    void ensureCollidingSolidsAtNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.solidsOverlapping(bounds));
    }

    @Test
    void testFullAgentCardinalCollisionWithFullBlocks() {
        testCardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock), true);
    }

    @Test
    void testFullAgentIntercardinalCollisionWithFullBlocks() {
        testIntercardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock), true);
    }

    @Test
    void testFullAgentCardinalCollisionWithTinyBlocks() {
        testCardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock), true);
    }

    @Test
    void testFullAgentIntercardinalCollisionWithTinyBlocks() {
        testIntercardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock), true);
    }

    @Test
    void testFullAgentNoCardinalCollision() {
        testCardinalSameCollision(fullAgentBounds, new ArrayList<>(), false);
    }

    @Test
    void testFullAgentNoIntercardinalCollision() {
        testIntercardinalSameCollision(fullAgentBounds, new ArrayList<>(), false);
    }

    @Test
    void testTinyAgentCardinalCollisionWithFullBlocks() {
        testCardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock), true);
    }

    @Test
    void testTinyAgentIntercardinalCollisionWithFullBlocks() {
        testIntercardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock), true);
    }

    @Test
    void testTinyAgentCardinalCollisionWithTinyBlocks() {
        testCardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock), true);
    }

    @Test
    void testTinyAgentIntercardinalCollisionWithTinyBlocks() {
        testIntercardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock), false);
    }

    @Test
    void testFullAgentNoCardinalCollisionInitialOverlapFullBlockAtWaist() {
        BlockCollisionView block = mockBlockAt(0, 0, 0, fullBlock, true);
        testCardinalSameCollision(fullAgentBounds, List.of(block), false);
    }

    @Test
    void testFullAgentNoIntercardinalCollisionInitialOverlapFullBlockAtWaist() {
        BlockCollisionView block = mockBlockAt(0, 0, 0, fullBlock, true);
        testIntercardinalSameCollision(fullAgentBounds, List.of(block), false);
    }

    @Test
    void testFullAgentNoCardinalCollisionInitialOverlapFullBlockAtWaistAndHead() {
        BlockCollisionView blockWaist = mockBlockAt(0, 0, 0, fullBlock, true);
        BlockCollisionView blockHead = mockBlockAt(0, 1, 0, fullBlock, true);
        testCardinalSameCollision(fullAgentBounds, List.of(blockWaist, blockHead), false);
    }

    @Test
    void testFullAgentNoIntercardinalCollisionInitialOverlapFullBlockAtWaistAndHead() {
        BlockCollisionView blockWaist = mockBlockAt(0, 0, 0, fullBlock, true);
        BlockCollisionView blockHead = mockBlockAt(0, 1, 0, fullBlock, true);
        testIntercardinalSameCollision(fullAgentBounds, List.of(blockWaist, blockHead), false);
    }

    @Test
    void testTallAgentNoCardinalCollisionInitialOverlapAtHeadStairs() {
        BoundingBox witherSkeleton = new BoundingBox(0.2, 0, 0.2, 0.8, 3, 0.8);

        BlockCollisionView blockHead = mockBlockAt(0, 2, 0, stairs, true);
        testCardinalSameCollision(witherSkeleton, List.of(blockHead), false);
    }
}