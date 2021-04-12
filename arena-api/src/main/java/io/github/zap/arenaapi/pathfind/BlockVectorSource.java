package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface BlockVectorSource extends ChunkVectorSource {
    int blockX();

    int blockY();

    int blockZ();

    @Override
    default int chunkX() {
        return blockX() >> 4;
    }

    @Override
    default int chunkZ() {
        return blockZ() >> 4;
    }

    static BlockVectorSource fromBlockCoordinate(int x, int y, int z) {
        return new BlockVectorSource() {
            @Override
            public int blockX() {
                return x;
            }

            @Override
            public int blockY() {
                return y;
            }

            @Override
            public int blockZ() {
                return z;
            }
        };
    }

    static BlockVectorSource fromBlockVector(@NotNull Vector vector) {
        return new BlockVectorSource() {
            @Override
            public int blockX() {
                return vector.getBlockX();
            }

            @Override
            public int blockY() {
                return vector.getBlockY();
            }

            @Override
            public int blockZ() {
                return vector.getBlockZ();
            }
        };
    }
}
