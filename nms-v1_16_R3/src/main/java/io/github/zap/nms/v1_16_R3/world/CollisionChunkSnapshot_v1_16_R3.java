package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BlockSnapshot;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.nms.common.world.WorldBridge;
import io.github.zap.vector.graph.ArrayChunkGraph;
import io.github.zap.vector.graph.ChunkGraph;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
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

    private static final Predicate<IBlockData> NON_SOLID_OR_PARTIAL = blockData -> {
        if(!blockData.getMaterial().isSolid()) {
            return true;
        }

        VoxelShape shape = shapeFromData(blockData);
        return (shape != VoxelShapes.fullCube() && shape != VoxelShapes.empty());
    };

    private static final IBlockData AIR_BLOCK_DATA = Blocks.AIR.getBlockData();
    private static final WorldBridge bridge = WorldBridge_v1_16_R3.INSTANCE;

    private final Chunk chunk;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final long captureFullTime;
    private final DataPaletteBlock<IBlockData>[] palette;
    private final ChunkGraph<BlockSnapshot> nonSolidOrPartial = new ArrayChunkGraph<>(0, 0, 1, 1);
    private final BoundingBox chunkBounds;
    private final int captureTick;

    CollisionChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        this.chunk = chunk;

        worldName = chunk.getWorld().getName();
        chunkX = chunk.getX();
        chunkZ = chunk.getZ();
        captureFullTime = chunk.getWorld().getFullTime();
        palette = loadFromChunk(((CraftChunk)chunk).getHandle());

        int originX = chunkX << 4;
        int originZ = chunkZ << 4;

        chunkBounds = new BoundingBox(originX, 0, originZ, originX + 16, 255, originZ + 16);
        captureTick = Bukkit.getCurrentTick();
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

                if(blocks.contains(NON_SOLID_OR_PARTIAL)) {
                    blocks.forEachLocation((blockData, position) -> {
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

                            examine.d(x, y, z);

                            VoxelShape shape = blockData.getCollisionShape(chunk, examine);
                            if(!blockData.getMaterial().isSolid() || (shape != VoxelShapes.empty() && shape != VoxelShapes.fullCube())) {
                                nonSolidOrPartial.putElement(x, y, z, BlockSnapshot.from((chunkX << 4) + x, y,
                                        (chunkZ << 4) + z, blockData.createCraftBlockData(), new VoxelShapeWrapper_v1_16_R3(shape)));
                            }
                        }
                    });
                }
            }
        }

        return sectionBlockIDs;
    }

    private static VoxelShape shapeFromData(IBlockData data) {
        return data.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
    }

    @Override
    public @NotNull BlockSnapshot collisionSnapshot(int chunkRelativeX, int chunkRelativeY, int chunkRelativeZ) {
        if(bridge.isValidChunkCoordinate(chunkRelativeX, chunkRelativeY, chunkRelativeZ)) {
            BlockSnapshot block = nonSolidOrPartial.elementAt(chunkRelativeX, chunkRelativeY, chunkRelativeZ);

            if(block != null) {
                return block;
            }
            else {
                IBlockData data = palette[chunkRelativeY >> 4].a(chunkRelativeX, chunkRelativeY & 15, chunkRelativeZ);

                return BlockSnapshot.from((chunkX << 4) + chunkRelativeX, chunkRelativeY, (chunkZ << 4) +
                                chunkRelativeZ, data.createCraftBlockData(), new VoxelShapeWrapper_v1_16_R3(shapeFromData(data)));
            }
        }

        throw new IllegalArgumentException("Chunk-relative coordinates out of range: [" + chunkRelativeX + ", " +
                chunkRelativeY + ", " + chunkRelativeZ + "]");
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldBounds) {
        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.clone().intersection(chunkBounds);
            SnapshotIterator iterator = new SnapshotIterator(overlap);

            while(iterator.hasNext()) {
                BlockSnapshot snapshot = iterator.next();

                if(snapshot.overlaps(overlap)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean collisionMatches(@NotNull BoundingBox worldRelativeBounds) {
        return false;
    }

    @Override
    public @NotNull List<BlockSnapshot> collisionsWith(@NotNull BoundingBox worldBounds) {
        List<BlockSnapshot> shapes = new ArrayList<>();

        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.clone().intersection(chunkBounds);
            SnapshotIterator iterator = new SnapshotIterator(overlap);

            while(iterator.hasNext()) {
                BlockSnapshot snapshot = iterator.next();

                if(snapshot.overlaps(overlap)) {
                    shapes.add(snapshot);
                }
            }
        }

        return shapes;
    }

    @Override
    public int captureTick() {
        return captureTick;
    }

    @Override
    public @NotNull Chunk chunk() {
        return chunk;
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
    @Deprecated
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
    @Deprecated
    public @NotNull Biome getBiome(int i, int i1) {
        throw new NotImplementedException("getBiome not implemented for CollisionChunkSnapshot!");
    }

    @Override
    public @NotNull Biome getBiome(int i, int i1, int i2) {
        throw new NotImplementedException("getBiome not implemented for CollisionChunkSnapshot!");
    }

    @Override
    @Deprecated
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

    private class SnapshotIterator implements Iterator<BlockSnapshot> {
        private final int startX;
        private final int startY;

        private final int endX;
        private final int endY;
        private final int endZ;

        private int x;
        private int y;
        private int z;

        private SnapshotIterator(BoundingBox overlap) {
            Vector min = overlap.getMin();
            Vector max = overlap.getMax();

            startX = min.getBlockX();
            startY = min.getBlockY();

            x = startX - 1;
            y = startY;
            z = min.getBlockZ();

            endX = max.getBlockX() + 1;
            endY = max.getBlockY() + 1;
            endZ = max.getBlockZ() + 1;
        }

        @Override
        public boolean hasNext() {
            int nextX = x + 1;
            int nextY = y;
            int nextZ = z;

            if(nextX == endX) {
                nextY++;
            }

            if(nextY == endY) {
                nextZ++;
            }

            return nextZ < endZ;
        }

        @Override
        public BlockSnapshot next() {
            if(++x == endX) {
                if(++y == endY) {
                    z++;
                    y = startY;
                }

                x = startX;
            }

            int chunkRelX = x & 15;
            int chunkRelZ = z & 15;

            BlockSnapshot snapshot = nonSolidOrPartial.elementAt(chunkRelX, y, chunkRelZ);

            if(snapshot == null) {
                IBlockData data = palette[y >> 4].a(chunkRelX,y & 15, chunkRelZ);
                snapshot = BlockSnapshot.from(x, y, z, data.createCraftBlockData(),
                        new VoxelShapeWrapper_v1_16_R3(shapeFromData(data)));
            }

            return snapshot;
        }
    }
}