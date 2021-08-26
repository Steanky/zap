package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.vector.Vector2I;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CollisionChunkView {
    @Nullable BlockCollisionView collisionView(int chunkX, int chunkY, int chunkZ);

    boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds);

    @NotNull List<BlockCollisionView> collisionsWith(@NotNull BoundingBox worldRelativeBounds);

    @NotNull Vector2I position();

    int captureTick();
}
