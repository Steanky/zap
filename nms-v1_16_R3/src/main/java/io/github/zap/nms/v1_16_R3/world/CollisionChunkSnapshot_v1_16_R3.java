package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BlockCollisionSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.nms.common.world.WorldBridge;
import io.github.zap.vector.VectorAccess;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.NotImplementedException;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

class CollisionChunkSnapshot_v1_16_R3 implements CollisionChunkSnapshot {
    private static final DataPaletteBlock<IBlockData> EMPTY_BLOCK_IDS =
            new ChunkSection(0, null, null, true).getBlocks();
    private static final Predicate<IBlockData> IS_PARTIAL_BLOCK = blockData ->
            blockData.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO) != VoxelShapes.fullCube();
    private static final IBlockData AIR_BLOCK_DATA = Blocks.AIR.getBlockData();
    private static final WorldBridge bridge = WorldBridge_v1_16_R3.INSTANCE;
    private static final BoundingBox CHUNK_BOUNDING_BOX = new BoundingBox(0, 0, 0, 16, 255, 16);

    private final String worldName;
    private final int worldX;
    private final int worldZ;
    private final int chunkX;
    private final int chunkZ;
    private final long captureFullTime;
    private final DataPaletteBlock<IBlockData>[] palette;
    private final Map<Long, BlockCollisionSnapshot> collisionMap = new HashMap<>();
    private final BoundingBox chunkBounds;

    CollisionChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        worldName = chunk.getWorld().getName();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        captureFullTime = chunk.getWorld().getFullTime();
        palette = loadFromChunk(((CraftChunk)chunk).getHandle());

        worldX = chunkX << 4;
        worldZ = chunkZ << 4;
        chunkBounds = new BoundingBox(worldX, 0, worldZ, worldX + 16, 255, worldZ + 16);
    }

    @Override
    public @NotNull BlockCollisionSnapshot blockCollisionSnapshot(int chunkRelativeX, int chunkRelativeY, int chunkRelativeZ) {
        if(bridge.isValidChunkCoordinate(chunkRelativeX, chunkRelativeY, chunkRelativeZ)) {
            return collisionMap.getOrDefault(org.bukkit.block.Block.getBlockKey(chunkRelativeX,
                    chunkRelativeY, chunkRelativeZ), BlockCollisionSnapshot.from(
                            VectorAccess.immutable(chunkRelativeX, chunkRelativeY, chunkRelativeZ),
                    getBlockData(chunkRelativeX, chunkRelativeY, chunkRelativeZ), VoxelShapeWrapper_v1_16_R3.FULL_BLOCK));
        }

        throw new IllegalArgumentException("Chunk-relative coordinates out of range: [" + chunkRelativeX + ", " +
                chunkRelativeY + ", " + chunkRelativeZ + "]");
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox relativeBounds) {
        if(relativeBounds.overlaps(CHUNK_BOUNDING_BOX)) {
            BoundingBox overlap = relativeBounds.intersection(chunkBounds);

            Vector min = overlap.getMin();
            Vector max = overlap.getMax();

            for(int x = min.getBlockX(); x < max.getBlockX(); x++) {
                for(int y = min.getBlockY(); y < max.getBlockY(); y++) {
                    for(int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                        BlockCollisionSnapshot snapshot = collisionMap.get(org.bukkit.block.Block.getBlockKey(x, y, z));

                        if(snapshot != null) {
                            if(snapshot.overlaps(relativeBounds)) {
                                return true;
                            }
                        }
                        else if(palette[y >> 4].a(x, y, z).getBukkitMaterial().isSolid()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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
                        Block.REGISTRY_ID, GameProfileSerializer::c, GameProfileSerializer::a, AIR_BLOCK_DATA,
                        null, false);
                blocks.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                sectionBlockIDs[i] = blocks;

                int yOffset = section.getYPosition();
                BlockPosition.MutableBlockPosition examine = BlockPosition.ZERO.i();

                if(blocks.contains(IS_PARTIAL_BLOCK)) {
                    blocks.forEachLocation((blockData, position) -> {
                        if(!blockData.isAir()) {
                             /*
                            bit operator magic:
                            (2^n) & (2^n - 1) == x % (2^n) for all positive integer values of n
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

                            examine.d(x, y, z);

                            VoxelShape voxelShape = blockData.getCollisionShape(chunk, examine);

                            if(voxelShape != VoxelShapes.fullCube()) {
                                collisionMap.put(org.bukkit.block.Block.getBlockKey(x, y, z), BlockCollisionSnapshot
                                        .from(VectorAccess.immutable(x, y, z),
                                                blockData.createCraftBlockData(), new VoxelShapeWrapper_v1_16_R3(voxelShape)));
                            }
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
        if(bridge.isValidChunkCoordinate(x, y, z)) {
            return (this.palette[y >> 4].a(x, y & 15, z)).getBukkitMaterial();
        }

        throw new IllegalArgumentException("Chunk-relative coordinates " + new Vector(x, y, z) + " out of bounds!");
    }

    @Override
    public @NotNull BlockData getBlockData(int x, int y, int z) {
        if(bridge.isValidChunkCoordinate(x, y, z)) {
            return CraftBlockData.fromData(this.palette[y >> 4].a(x, y & 15, z));
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
        for(DataPaletteBlock<IBlockData> palette : palette) {
            if(palette.contains(nms)) {
                return true;
            }
        }

        return false;
    }
}
