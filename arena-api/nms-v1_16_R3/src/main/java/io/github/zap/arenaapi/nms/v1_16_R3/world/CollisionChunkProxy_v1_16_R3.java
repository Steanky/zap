package io.github.zap.arenaapi.nms.v1_16_R3.world;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CollisionChunkProxy_v1_16_R3 extends CollisionChunkAbstract_v1_16_R3 {
    private static final Map<VoxelShape, VoxelShapeWrapper> shapeMap = Collections.synchronizedMap(new IdentityHashMap<>());

    private final WeakReference<Chunk> chunk;
    private final Cache<Integer, BlockCollisionView> collisionViewCache;

    CollisionChunkProxy_v1_16_R3(@NotNull Chunk chunk, int expectedConcurrency, long writeTimeout,
                                 @NotNull TimeUnit timeoutUnit) {
        super(chunk.locX, chunk.locZ);
        this.chunk = new WeakReference<>(chunk);
        collisionViewCache = CacheBuilder.newBuilder()
                .concurrencyLevel(expectedConcurrency)
                .expireAfterWrite(writeTimeout, timeoutUnit)
                .softValues()
                .build();
    }


    //pack 3 ints into a single int. produces unique outputs for all x, y, and z on the interval [0, 256)
    private int blockKey(int x, int y, int z) {
        return (z | (y << 8)) | (x << 16);
    }

    @Override
    public BlockCollisionView collisionView(int chunkX, int chunkY, int chunkZ) {
        Chunk currentChunk = this.chunk.get();

        if(currentChunk != null) {
            BlockCollisionView collision = collisionViewCache.getIfPresent(blockKey(chunkX, chunkY, chunkZ));

            if(collision != null) {
                return collision;
            }
            else {
                ChunkSection[] sections = currentChunk.getSections();
                ChunkSection section = sections[chunkY >> 4];

                VoxelShapeWrapper wrapper;
                BlockData bukkitData;
                if(section != null && !section.c()) {
                    IBlockData data = section.getType(chunkX, chunkY & 15, chunkZ);
                    if (data.getBukkitMaterial() == org.bukkit.Material.SHULKER_BOX) {
                        return BlockCollisionView.from((x << 4) + chunkX, chunkY, (z << 4) + chunkZ,
                                data.createCraftBlockData(), VoxelShapeWrapper_v1_16_R3.FULL);
                    }

                    VoxelShape voxelShape = data.getCollisionShape(currentChunk, new BlockPosition(chunkX, chunkY, chunkZ));
                    wrapper = shapeMap.computeIfAbsent(voxelShape, (key) -> new VoxelShapeWrapper_v1_16_R3(voxelShape));
                    bukkitData = data.createCraftBlockData();
                }
                else {
                    wrapper = VoxelShapeWrapper_v1_16_R3.EMPTY;
                    bukkitData = org.bukkit.Material.AIR.createBlockData();
                }

                BlockCollisionView collisionView = BlockCollisionView.from((x << 4) + chunkX, chunkY,
                        (z << 4) + chunkZ, bukkitData, wrapper);
                collisionViewCache.put(blockKey(chunkX, chunkY, chunkZ), collisionView);
                return collisionView;
            }
        }

        return null;
    }

    @Override
    public int captureTick() {
        return Bukkit.getCurrentTick();
    }
}
