package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.SimpleChunkSnapshot;
import io.github.zap.nms.common.world.WrappedVoxelShape;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.NotImplementedException;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

class SimpleChunkSnapshot_v1_16_R3 implements SimpleChunkSnapshot, ChunkSnapshot {
    private static final DataPaletteBlock<IBlockData> emptyBlockIDs = (new ChunkSection(0, null, null, true)).getBlocks();
    private static final Predicate<IBlockData> partialBlock = blockData ->
            isPartialSolidBlock(blockData.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO).d());

    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final long captureFullTime;
    private final DataPaletteBlock<IBlockData>[] blockids;
    private final Map<Long, WrappedVoxelShape> collisionMap = new HashMap<>();

    SimpleChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        worldName = chunk.getWorld().getName();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        captureFullTime = chunk.getWorld().getFullTime();
        blockids = loadFromChunk(((CraftChunk)chunk).getHandle());
    }

    @Override
    public @Nullable WrappedVoxelShape collisionFor(int chunkX, int chunkY, int chunkZ) {
        return collisionMap.get(org.bukkit.block.Block.getBlockKey(chunkX, chunkY, chunkZ));
    }

    private static boolean isUnit(AxisAlignedBB aabb) {
        double x = aabb.maxX - aabb.minX;
        double y = aabb.maxY - aabb.minY;
        double z = aabb.maxZ - aabb.minZ;

        return x == 1 && y == 1 && z == 1;
    }

    private static boolean isPartialSolidBlock(List<AxisAlignedBB> bounds) {
        int size = bounds.size();

        if(size == 1) {
            return !isUnit(bounds.get(0));
        }

        return size > 0;
    }

    private DataPaletteBlock<IBlockData>[] loadFromChunk(net.minecraft.server.v1_16_R3.Chunk chunk) {
        ChunkSection[] sections = chunk.getSections();
        //noinspection unchecked
        DataPaletteBlock<IBlockData>[] sectionBlockIDs = new DataPaletteBlock[sections.length];

        for(int i = 0; i < sections.length; ++i) {
            ChunkSection section = sections[i];
            if (section == null) {
                sectionBlockIDs[i] = emptyBlockIDs;
            } else {
                NBTTagCompound data = new NBTTagCompound();
                section.getBlocks().a(data, "Palette", "BlockStates");
                DataPaletteBlock<IBlockData> blockids = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE,
                        Block.REGISTRY_ID, GameProfileSerializer::c, GameProfileSerializer::a,
                        Blocks.AIR.getBlockData(), null, false);
                blockids.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                sectionBlockIDs[i] = blockids;

                int yOffset = section.getYPosition();
                if(blockids.contains(partialBlock)) {
                    blockids.forEachLocation((blockData, position) -> {
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

                        BlockPosition pos = new BlockPosition(x, y, z);
                        List<AxisAlignedBB> shape = blockData.getCollisionShape(chunk, pos).d();

                        if(isPartialSolidBlock(shape)) {
                            collisionMap.put(org.bukkit.block.Block.getBlockKey(x, y, z), new WrappedVoxelShape_v1_16_R3(shape));
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
    public org.bukkit.@NotNull Material getBlockType(int x, int y, int z) {
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
        throw new NotImplementedException("getData not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public int getBlockSkyLight(int i, int i1, int i2) {
        throw new NotImplementedException("getBlockSkyLight not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public int getBlockEmittedLight(int i, int i1, int i2) {
        throw new NotImplementedException("getBlockEmittedLight not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public int getHighestBlockYAt(int i, int i1) {
        throw new NotImplementedException("getHighestBlockYAt not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public @NotNull Biome getBiome(int i, int i1) {
        throw new NotImplementedException("getBiome not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public @NotNull Biome getBiome(int i, int i1, int i2) {
        throw new NotImplementedException("getBiome not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public double getRawBiomeTemperature(int i, int i1) {
        throw new NotImplementedException("getRawBiomeTemperature not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public double getRawBiomeTemperature(int i, int i1, int i2) {
        throw new NotImplementedException("getRawBiomeTemperature not implemented for SimpleChunkSnapshot!");
    }

    @Override
    public long getCaptureFullTime() {
        return captureFullTime;
    }

    @Override
    public boolean isSectionEmpty(int i) {
        throw new NotImplementedException("isSectionEmpty not implemented for SimpleChunkSnapshot!");
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
