package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

class ProxyBlockCollisionProviderTest {
    @FunctionalInterface
    private interface ChunkCoordinateConsumer {
        void consume(int x, int z);
    }

    private static final int LOWER_CHUNK_BOUND = -5;
    private static final int UPPER_CHUNK_BOUND = 5;

    private WorldBridge bridge;
    private World world;
    private ProxyBlockCollisionProvider provider;

    //add chunks to this to prevent them from being GCed during testing
    private final List<CollisionChunkView> strongRefs = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = Mockito.mock(WorldBridge.class);
        world = Mockito.mock(World.class);

        initMockChunks();

        provider = new ProxyBlockCollisionProvider(bridge, world, 1);
    }

    private void iterateMockChunks(@NotNull ChunkCoordinateConsumer consumer) {
        for(int cX = LOWER_CHUNK_BOUND; cX <= UPPER_CHUNK_BOUND; cX++) {
            for(int cZ = LOWER_CHUNK_BOUND; cZ <= UPPER_CHUNK_BOUND; cZ++) {
                consumer.consume(cX, cZ);
            }
        }
    }

    private void initMockChunks() {
        iterateMockChunks((x, z) -> {
            Mockito.when(world.isChunkLoaded(x, z)).thenReturn(true);

            Chunk chunk = Mockito.mock(Chunk.class);
            Mockito.when(bridge.getChunkIfLoadedImmediately(world, x, z)).thenReturn(chunk);

            CollisionChunkView collisionChunkView = Mockito.mock(CollisionChunkView.class);
            strongRefs.add(collisionChunkView);
            Mockito.when(collisionChunkView.position()).thenReturn(Vectors.of(x, z));

            Mockito.when(bridge.proxyView(chunk)).thenReturn(collisionChunkView);
        });
    }

    @Test
    void hasChunk() {
        iterateMockChunks((x, z) -> Assertions.assertTrue(provider.hasChunk(x, z)));
    }

    @Test
    void chunkAtNonNull() {
        iterateMockChunks((x, z) -> Assertions.assertNotNull(provider.chunkAt(x, z)));
    }

    @Test
    void chunkAtMatchesPosition() {
        iterateMockChunks((x, z) -> {
            CollisionChunkView collisionChunkView = provider.chunkAt(x, z);
            Assertions.assertNotNull(collisionChunkView);

            Vector2I pos = collisionChunkView.position();
            Assertions.assertTrue(pos.x() == x && pos.z() == z);
        });
    }
}