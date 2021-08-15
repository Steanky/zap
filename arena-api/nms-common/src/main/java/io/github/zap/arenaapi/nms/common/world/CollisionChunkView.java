package io.github.zap.arenaapi.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CollisionChunkView {
    @Nullable BlockSnapshot collisionView(int chunkX, int chunkY, int chunkZ);

    boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds);

    @NotNull List<BlockSnapshot> collisionsWith(@NotNull BoundingBox worldRelativeBounds);

    int captureTick();
}
