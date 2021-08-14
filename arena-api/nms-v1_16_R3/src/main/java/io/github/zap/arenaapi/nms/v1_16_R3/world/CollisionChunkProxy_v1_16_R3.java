package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BlockSnapshot;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class CollisionChunkProxy_v1_16_R3 extends CollisionChunkAbstract_v1_16_R3 {
    private final WeakReference<Chunk> chunk;
    private final BlockSnapshotFactory factory;

    CollisionChunkProxy_v1_16_R3(@NotNull Chunk chunk) {
        super(chunk);
        this.chunk = new WeakReference<>(chunk);
        this.factory = (chunkX, chunkY, chunkZ) -> {
            assertValidChunkCoordinate(chunkX, chunkY, chunkZ);

            Chunk currentChunk = this.chunk.get();
            if(currentChunk != null) {
                ChunkSection[] sections = currentChunk.getSections();
                int segmentY = chunkY >> 4;

                ChunkSection section = sections[segmentY];

                if(section != null && !section.c()) {
                    IBlockData data = section.getType(chunkX, chunkY & 15, chunkZ);
                    VoxelShape voxelShape = data.getCollisionShape(chunk, new BlockPosition(chunkX, chunkY, chunkZ));
                    VoxelShapeWrapper wrapper = new VoxelShapeWrapper_v1_16_R3(voxelShape);

                    return BlockSnapshot.from((chunkX << 4) + chunkX, chunkY, (chunkZ << 4) +
                            chunkZ, data.createCraftBlockData(), wrapper);
                }
            }

            return null;
        };
    }

    @Override
    public int captureTick() {
        return Bukkit.getServer().getCurrentTick();
    }

    @Override
    protected @NotNull BlockSnapshotFactory getSnapshotFactory() {
        return factory;
    }
}
