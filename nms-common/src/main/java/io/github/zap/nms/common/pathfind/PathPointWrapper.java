package io.github.zap.nms.common.pathfind;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PathPointWrapper extends Vector3I {
    @Nullable PathPointWrapper parent();

    void setParent(@Nullable PathPointWrapper parent);
}
