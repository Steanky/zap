package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProxyBlockCollisionProviderTest {
    private WorldBridge bridge;
    private World world;
    private ProxyBlockCollisionProvider provider;

    @BeforeEach
    void setUp() {
        bridge = Mockito.mock(WorldBridge.class);
        world = Mockito.mock(World.class);
        Mockito.when(bridge.getDefaultWorldName()).thenReturn("mock");
        Chunk chunk = Mockito.mock(Chunk.class);
        Mockito.when(bridge.getChunkIfLoadedImmediately(world, 0, 0)).thenReturn(chunk);

        CollisionChunkView collisionChunkView = Mockito.mock(CollisionChunkView.class);
        Mockito.when(bridge.proxyView(chunk)).thenReturn(collisionChunkView);

        provider = new ProxyBlockCollisionProvider(bridge, world, 1);
    }

    @Test
    void hasChunk() {
    }

    @Test
    void chunkAt() {
    }
}