package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.NotImplementedException;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

class CollisionChunkSnapshot_v1_16_R3 implements CollisionChunkSnapshot {
    private static final DataPaletteBlock<IBlockData> EMPTY_BLOCK_IDS = (new ChunkSection(0, null, null, true)).getBlocks();
    private static final Predicate<IBlockData> IS_PARTIAL_BLOCK = blockData ->
            blockData.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO) != VoxelShapes.fullCube();
    private static final IBlockData AIR_BLOCK_DATA = Blocks.AIR.getBlockData();

    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final long captureFullTime;
    private final DataPaletteBlock<IBlockData>[] blockids;
    private final Map<Long, BlockCollisionSnapshot> collisionMap = new HashMap<>();

    CollisionChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        worldName = chunk.getWorld().getName();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        captureFullTime = chunk.getWorld().getFullTime();
        blockids = loadFromChunk(((CraftChunk)chunk).getHandle());
    }

    @Override
    public @Nullable BlockCollisionSnapshot blockCollisionSnapshot(int chunkRelativeX, int chunkRelativeY, int chunkRelativeZ) {
        return collisionMap.getOrDefault(org.bukkit.block.Block.getBlockKey(chunkRelativeX, chunkRelativeY, chunkRelativeZ),
                BlockCollisionSnapshot.from(getBlockData(chunkRelativeX, chunkRelativeY, chunkRelativeZ),
                        VoxelShapeWrapper_v1_16_R3.FULL_BLOCK));
    }

    private static boolean isUnit(AxisAlignedBB aabb) {
        double x = aabb.maxX - aabb.minX;
        double y = aabb.maxY - aabb.minY;
        double z = aabb.maxZ - aabb.minZ;

        return x == 1 && y == 1 && z == 1;
    }

    private DataPaletteBlock<IBlockData>[] loadFromChunk(net.minecraft.server.v1_16_R3.Chunk chunk) {
        ChunkSection[] sections = chunk.getSections();
        //noinspection unchecked
        DataPaletteBlock<IBlockData>[] sectionBlockIDs = new DataPaletteBlock[sections.length];

        for(int i = 0; i < sections.length; ++i) {
            ChunkSection section = sections[i];
            if (section == null) {
                sectionBlockIDs[i] = EMPTY_BLOCK_IDS;
            } else {
                NBTTagCompound data = new NBTTagCompound();
                section.getBlocks().a(data, "Palette", "BlockStates");
                DataPaletteBlock<IBlockData> blocks = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE,
                        Block.REGISTRY_ID, GameProfileSerializer::c, GameProfileSerializer::a,
                        AIR_BLOCK_DATA, null, false);
                blocks.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                sectionBlockIDs[i] = blocks;

                int yOffset = section.getYPosition();
                BlockPosition examine = new BlockPosition(0, 0, 0);

                if(blocks.contains(IS_PARTIAL_BLOCK)) {
                    blocks.forEachLocation((blockData, position) -> {
                        /*
                        bit operator magic:
                        (2^n) & (2^n - 1) == x % (2^n) for all positive integer values of n
                        x >> n == x / 2^n for all positive integer n

                        translation:
                        int x = position % 16;
                        int y = (position / 256) + yOffset;
                        int z = (position % 256) / 16;
                         */
                        int x = position & 15;
                        int y = (position >> 8) + yOffset;
                        int z = (position & 255) >> 4;

                        examine.o(x);
                        examine.p(y);
                        examine.q(z);

                        VoxelShape voxelShape = blockData.getCollisionShape(chunk, examine);

                        if(voxelShape != VoxelShapes.fullCube()) {
                            collisionMap.put(org.bukkit.block.Block.getBlockKey(x, y, z),
                                    BlockCollisionSnapshot.from(blockData.createCraftBlockData(),
                                            new VoxelShapeWrapper_v1_16_R3(voxelShape)));
                        }
                    });
                }
            }
        }

        return sectionBlockIDs;
    }

    @Override
    public int getX() {
        return chunkX;
    }

    @Override
    public int getZ() {
        return chunkZ;
    }

    @Override
    public @NotNull String getWorldName() {
        return worldName;
    }

    @Override
    public @NotNull org.bukkit.Material getBlockType(int x, int y, int z) {
        if(WorldBridge_v1_16_R3.INSTANCE.isValidChunkCoordinate(x, y, z)) {
            return (this.blockids[y >> 4].a(x, y & 15, z)).getBukkitMaterial();
        }

        throw new IllegalArgumentException("Chunk-relative coordinates " + new Vector(x, y, z) + " out of bounds!");
    }

    @Override
    public @NotNull BlockData getBlockData(int x, int y, int z) {
        if(WorldBridge_v1_16_R3.INSTANCE.isValidChunkCoordinate(x, y, z)) {
            return CraftBlockData.fromData(this.blockids[y >> 4].a(x, y & 15, z));
        }

        throw new IllegalArgumentException("Chunk-relative coordinates " + new Vector(x, y, z) + " out of bounds!");
    }

    @Override
    public int getData(int i, int i1, int i2) {
        throw new NotImplementedException("getData not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public int getBlockSkyLight(int i, int i1, int i2) {
        throw new NotImplementedException("getBlockSkyLight not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public int getBlockEmittedLight(int i, int i1, int i2) {
        throw new NotImplementedException("getBlockEmittedLight not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public int getHighestBlockYAt(int i, int i1) {
        throw new NotImplementedException("getHighestBlockYAt not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public @NotNull Biome getBiome(int i, int i1) {
        throw new NotImplementedException("getBiome not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public @NotNull Biome getBiome(int i, int i1, int i2) {
        throw new NotImplementedException("getBiome not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public double getRawBiomeTemperature(int i, int i1) {
        throw new NotImplementedException("getRawBiomeTemperature not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public double getRawBiomeTemperature(int i, int i1, int i2) {
        throw new NotImplementedException("getRawBiomeTemperature not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public long getCaptureFullTime() {
        return captureFullTime;
    }

    @Override
    public boolean isSectionEmpty(int i) {
        throw new NotImplementedException("isSectionEmpty not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public boolean contains(@NotNull BlockData blockData) {
        Objects.requireNonNull(blockData, "Block cannot be null");
        Predicate<IBlockData> nms = iBlockData -> Objects.equals(iBlockData, ((CraftBlockData) blockData).getState());
        for(DataPaletteBlock<IBlockData> palette : blockids) {
            if(palette.contains(nms)) {
                return true;
            }
        }

        return false;
    }
}
