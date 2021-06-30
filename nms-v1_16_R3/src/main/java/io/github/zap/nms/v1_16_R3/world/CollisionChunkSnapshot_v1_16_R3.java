package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.nms.common.world.WorldBridge;
import io.github.zap.vector.VectorAccess;
import io.github.zap.vector.util.VectorHelper;
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
    private static final Predicate<IBlockData> IS_PARTIAL_BLOCK = blockData -> {
        VoxelShape shape = blockData.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
        return shape != VoxelShapes.fullCube() && shape != VoxelShapes.empty();
    };

    private static final IBlockData AIR_BLOCK_DATA = Blocks.AIR.getBlockData();
    private static final WorldBridge bridge = WorldBridge_v1_16_R3.INSTANCE;

    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final long captureFullTime;
    private final DataPaletteBlock<IBlockData>[] palette;
    private final Map<Long, BlockSnapshot> collisionMap = new HashMap<>();
    private final BoundingBox chunkBounds;

    CollisionChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        worldName = chunk.getWorld().getName();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        captureFullTime = chunk.getWorld().getFullTime();
        palette = loadFromChunk(((CraftChunk)chunk).getHandle());

        int originX = chunkX << 4;
        int originZ = chunkZ << 4;

        chunkBounds = new BoundingBox(originX, 0, originZ, originX + 16, 255, originZ + 16);
    }

    @Override
    public @NotNull BlockSnapshot collisionSnapshot(int chunkRelativeX, int chunkRelativeY, int chunkRelativeZ) {
        if(bridge.isValidChunkCoordinate(chunkRelativeX, chunkRelativeY, chunkRelativeZ)) {
            BlockSnapshot block = collisionMap.get(org.bukkit.block.Block.getBlockKey(chunkRelativeX,
                    chunkRelativeY, chunkRelativeZ));

            if(block != null) {
                return block;
            }
            else {
                IBlockData data = palette[chunkRelativeY >> 4].a(chunkRelativeX, chunkRelativeY & 15, chunkRelativeZ);
                if(data.getBukkitMaterial().isSolid()) {
                    return BlockSnapshot.from(VectorHelper.toWorldRelative(VectorAccess.immutable(chunkRelativeX,
                            chunkRelativeY, chunkRelativeZ), chunkX, chunkZ), data.createCraftBlockData(),
                            VoxelShapeWrapper.FULL_BLOCK);
                }
                else {
                    return BlockSnapshot.from(VectorHelper.toWorldRelative(VectorAccess.immutable(chunkRelativeX,
                            chunkRelativeY, chunkRelativeZ), chunkX, chunkZ), data.createCraftBlockData(),
                            VoxelShapeWrapper.EMPTY_BLOCK);
                }
            }
        }

        throw new IllegalArgumentException("Chunk-relative coordinates out of range: [" + chunkRelativeX + ", " +
                chunkRelativeY + ", " + chunkRelativeZ + "]");
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldBounds) {
        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.intersection(chunkBounds);

            Vector min = overlap.getMin();
            Vector max = overlap.getMax();

            for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for(int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        int chunkX = x & 15;
                        int chunkZ = z & 15;

                        BlockSnapshot snapshot = collisionMap.get(org.bukkit.block.Block.getBlockKey(chunkX, y, chunkZ));

                        if(snapshot != null) {
                            if(snapshot.overlaps(worldBounds)) {
                                return true;
                            }
                        }
                        else if(palette[y >> 4].a(chunkX, y & 15, chunkZ).getBukkitMaterial().isSolid()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<BlockSnapshot> collisionsWith(@NotNull BoundingBox worldBounds) {
        List<BlockSnapshot> shapes = new ArrayList<>();

        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.intersection(chunkBounds);

            Vector min = overlap.getMin();
            Vector max = overlap.getMax();

            for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for(int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        int chunkX = x & 15;
                        int chunkZ = z & 15;

                        BlockSnapshot snapshot = collisionMap.get(org.bukkit.block.Block.getBlockKey(chunkX, y, chunkZ));

                        if(snapshot != null) {
                            if(snapshot.overlaps(worldBounds)) {
                                shapes.add(snapshot);
                            }
                        }
                        else {
                            BlockBase.BlockData data = palette[y >> 4].a(chunkX, y & 15, chunkZ);

                            if(data.getMaterial().isSolid()) {
                                shapes.add(BlockSnapshot.from(VectorAccess.immutable(x, y, z),
                                        data.createCraftBlockData(), VoxelShapeWrapper.FULL_BLOCK));
                            }
                        }
                    }
                }
            }
        }

        return shapes;
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
                section.getBlocks().a(data, "Palette", "BlockStates"); //fill up nbt data for this segment

                DataPaletteBlock<IBlockData> blocks = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE,
                        Block.REGISTRY_ID, GameProfileSerializer::c, GameProfileSerializer::a, AIR_BLOCK_DATA,
                        null, false);

                //load nbt data to palette
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
                                collisionMap.put(org.bukkit.block.Block.getBlockKey(x, y, z),
                                        BlockSnapshot.from(VectorHelper.toWorldRelative(VectorAccess.immutable(x, y, z),
                                                chunkX, chunkZ), blockData.createCraftBlockData(),
                                                new VoxelShapeWrapper_v1_16_R3(voxelShape)));
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
