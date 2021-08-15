package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BlockSnapshot;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class CollisionChunkProxy_v1_16_R3 extends CollisionChunkAbstract_v1_16_R3 {
    private final WeakReference<Chunk> chunk;

    CollisionChunkProxy_v1_16_R3(@NotNull Chunk chunk) {
        super(chunk);
        this.chunk = new WeakReference<>(chunk);
    }

    @Override
    protected BlockSnapshot makeSnapshot(int chunkX, int chunkY, int chunkZ) {
        Chunk currentChunk = this.chunk.get();
        if(currentChunk != null) {
            ChunkSection[] sections = currentChunk.getSections();
            ChunkSection section = sections[chunkY >> 4];

            if(section != null && !section.c()) {
                IBlockData data = section.getType(chunkX, chunkY & 15, chunkZ);
                if (data.getBukkitMaterial() == org.bukkit.Material.SHULKER_BOX) {
                    return BlockSnapshot.from((x << 4) + chunkX, chunkY, (z << 4) +
                            chunkZ, data.createCraftBlockData(), VoxelShapeWrapper_v1_16_R3.FULL);
                }

                VoxelShape voxelShape = data.getCollisionShape(currentChunk, new BlockPosition(chunkX, chunkY, chunkZ));
                VoxelShapeWrapper wrapper;

                if(voxelShape == VoxelShapes.fullCube()) {
                    wrapper = VoxelShapeWrapper_v1_16_R3.FULL;
                }
                else if(voxelShape == VoxelShapes.empty()) {
                    wrapper = VoxelShapeWrapper_v1_16_R3.EMPTY;
                }
                else {
                    wrapper = new VoxelShapeWrapper_v1_16_R3(voxelShape);
                }

                return BlockSnapshot.from((x << 4) + chunkX, chunkY, (z << 4) +
                        chunkZ, data.createCraftBlockData(), wrapper);
            }
        }

        return null;
    }

    @Override
    public int captureTick() {
        return Bukkit.getCurrentTick();
    }
}
