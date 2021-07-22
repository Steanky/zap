package io.github.zap.arenaapi.v1_17_R1.world;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.vector.graph.ArrayChunkGraph;
import io.github.zap.vector.graph.ChunkGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

class CollisionChunkSnapshot_v1_17_R1 extends CollisionChunkAbstract_v1_17_R1 {
    private static final PalettedContainer<BlockState> EMPTY_BLOCK_IDS =
            new LevelChunkSection(0, null, null, true).getStates();

    private static final Predicate<BlockState> NON_SOLID_OR_PARTIAL = blockData -> {
        if(!blockData.getMaterial().isSolid()) {
            return true;
        }

        VoxelShape shape = shapeFromData(blockData);
        return (shape != Shapes.block() && shape != Shapes.empty());
    };

    private static final BlockState AIR_BLOCK_DATA = Blocks.AIR.defaultBlockState();

    private final PalettedContainer<BlockState>[] palette;
    private final ChunkGraph<BlockCollisionView> nonSolidOrPartial = new ArrayChunkGraph<>(0, 0, 1, 1);
    private final int captureTick;

    CollisionChunkSnapshot_v1_17_R1(@NotNull LevelChunk chunk) {
        super(chunk);

        palette = loadFromChunk(chunk);
        captureTick = Bukkit.getCurrentTick();
    }

    @Override
    protected BlockCollisionView makeSnapshot(int chunkX, int chunkY, int chunkZ) {
        BlockCollisionView snapshot = nonSolidOrPartial.elementAt(chunkX, chunkY, chunkZ);

        if(snapshot == null) {
            BlockState data = palette[chunkY >> 4].get(chunkX, chunkY & 15, chunkZ);

            snapshot = BlockCollisionView.from(originX + chunkX, chunkY, originZ + chunkZ,
                    data.createCraftBlockData(), new VoxelShapeWrapper_v1_17_R1(shapeFromData(data)));
        }

        return snapshot;
    }

    private PalettedContainer<BlockState>[] loadFromChunk(LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        //noinspection unchecked
        PalettedContainer<BlockState>[] sectionBlockIDs = new PalettedContainer[sections.length];

        for(int i = 0; i < sections.length; ++i) {
            LevelChunkSection section = sections[i];

            if (section == null) {
                sectionBlockIDs[i] = EMPTY_BLOCK_IDS;
            } else {
                CompoundTag data = new CompoundTag();
                section.getStates().write(data, "Palette", "BlockStates"); //fill up nbt data for this segment

                PalettedContainer<BlockState> blocks
                        = new PalettedContainer<>(LevelChunkSection.GLOBAL_BLOCKSTATE_PALETTE,
                        Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, AIR_BLOCK_DATA,
                        null, false);

                //load nbt data to palette
                blocks.read(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                sectionBlockIDs[i] = blocks;

                int yOffset = section.bottomBlockY();
                BlockPos.MutableBlockPos examine = BlockPos.ZERO.mutable();

                if(blocks.maybeHas(NON_SOLID_OR_PARTIAL)) {
                    blocks.count((blockData, position) -> {
                        if(!blockData.isAir()) {
                            /*
                            bit operator magic:
                            x & (2^n - 1) == x % (2^n) for all positive integer values of n
                            x >> n == x / 2^n for all positive integer n

                            translation:
                            int x = position % 16;
                            int y = (position / 256) + yOffset;
                            int z = (position % 256) / 16;

                            these coordinates are chunk-relative
                             */
                            int x = position & 15;
                            int y = (position >> 8) + yOffset;
                            int z = (position & 255) >> 4;

                            examine.set(x, y, z);

                            VoxelShape shape = blockData.getCollisionShape(chunk, examine);
                            if(!blockData.getMaterial().isSolid() || (shape != Shapes.empty() && shape != Shapes.block())) {
                                nonSolidOrPartial.putElement(x, y, z, BlockCollisionView.from(originX + x, y,
                                        originZ + z, blockData.createCraftBlockData(), new VoxelShapeWrapper_v1_17_R1(shape)));
                            }
                        }
                    });
                }
            }
        }

        return sectionBlockIDs;
    }

    private static VoxelShape shapeFromData(BlockState data) {
        return data.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    @Override
    public int captureTick() {
        return captureTick;
    }
}
