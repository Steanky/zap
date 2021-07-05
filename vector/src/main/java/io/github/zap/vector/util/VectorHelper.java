package io.github.zap.vector.util;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

public class VectorHelper {
    public static ImmutableWorldVector toChunkRelative(@NotNull VectorAccess worldRelativeVector) {
        return VectorAccess.immutable((worldRelativeVector.blockX() & 15) + (worldRelativeVector.x() -
                worldRelativeVector.blockX()), worldRelativeVector.y(), (worldRelativeVector.blockZ() & 15) +
                (worldRelativeVector.z() - worldRelativeVector.blockZ()));
    }

    public static ImmutableWorldVector toWorldRelative(@NotNull VectorAccess chunkRelativeVector, int chunkX, int chunkZ) {
        return VectorAccess.immutable( (chunkX << 4) + chunkRelativeVector.x(), chunkRelativeVector.y(),
                (chunkZ << 4) + chunkRelativeVector.z());
    }
}
