package io.github.zap.arenaapi.nms.v1_16_R3.world;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.RecordBuilder;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class CollisionChunkProxy_v1_16_R3 extends CollisionChunkAbstract_v1_16_R3 {
    private static final Map<VoxelShape, VoxelShapeWrapper> shapeMap = Collections.synchronizedMap(new IdentityHashMap<>());

    private final WeakReference<Chunk> chunk;

    CollisionChunkProxy_v1_16_R3(@NotNull Chunk chunk) {
        super(chunk.locX, chunk.locZ);
        this.chunk = new WeakReference<>(chunk);
    }

    @Override
    public BlockCollisionView collisionView(int chunkX, int chunkY, int chunkZ) {
        assertValidChunkCoordinate(chunkX, chunkY, chunkZ);

        Chunk currentChunk = this.chunk.get();
        if(currentChunk != null) {
            ChunkSection[] sections = currentChunk.getSections();
            ChunkSection section = sections[chunkY >> 4];

            if(section != null && !section.c()) {
                IBlockData data = section.getType(chunkX, chunkY & 15, chunkZ);
                if (data.getBukkitMaterial() == org.bukkit.Material.SHULKER_BOX) {
                    return BlockCollisionView.from((x << 4) + chunkX, chunkY, (z << 4) +
                            chunkZ, data.createCraftBlockData(), VoxelShapeWrapper_v1_16_R3.FULL);
                }

                VoxelShape voxelShape = data.getCollisionShape(currentChunk, new BlockPosition(chunkX, chunkY, chunkZ));
                VoxelShapeWrapper wrapper = shapeMap.computeIfAbsent(voxelShape, (key) ->
                        new VoxelShapeWrapper_v1_16_R3(voxelShape));

                return BlockCollisionView.from((x << 4) + chunkX, chunkY, (z << 4) + chunkZ,
                        data.createCraftBlockData(), wrapper);
            }
        }

        return null;
    }

    @Override
    public int captureTick() {
        return Bukkit.getCurrentTick();
    }
}
