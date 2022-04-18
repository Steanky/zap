package io.github.zap.arenaapi.v1_17_R1.world;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class CollisionChunkProxy_v1_17_R1 extends CollisionChunkAbstract_v1_17_R1 {
    private final WeakReference<LevelChunk> chunk;

    CollisionChunkProxy_v1_17_R1(@NotNull LevelChunk chunk) {
        super(chunk);
        this.chunk = new WeakReference<>(chunk);
    }

    @Override
    protected BlockCollisionView makeSnapshot(int chunkX, int chunkY, int chunkZ) {
        LevelChunk currentChunk = this.chunk.get();
        if(currentChunk != null) {
            LevelChunkSection[] sections = currentChunk.getSections();
            LevelChunkSection section = sections[chunkY >> 4];

            if(section != null && !section.isEmpty()) {
                BlockState data = section.getBlockState(chunkX, chunkY & 15, chunkZ);
                if (data.getBukkitMaterial() == org.bukkit.Material.SHULKER_BOX) {
                    return BlockCollisionView.from((x << 4) + chunkX, chunkY, (z << 4) +
                            chunkZ, data.createCraftBlockData(), VoxelShapeWrapper_v1_17_R1.FULL);
                }

                VoxelShape voxelShape = data.getCollisionShape(currentChunk, new BlockPos(chunkX, chunkY, chunkZ));
                VoxelShapeWrapper wrapper;

                if(voxelShape == Shapes.block()) {
                    wrapper = VoxelShapeWrapper_v1_17_R1.FULL;
                }
                else if(voxelShape == Shapes.empty()) {
                    wrapper = VoxelShapeWrapper_v1_17_R1.EMPTY;
                }
                else {
                    wrapper = new VoxelShapeWrapper_v1_17_R1(voxelShape);
                }

                return BlockCollisionView.from((x << 4) + chunkX, chunkY, (z << 4) +
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
