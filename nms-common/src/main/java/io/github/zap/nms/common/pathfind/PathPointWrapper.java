package io.github.zap.nms.common.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PathPointWrapper {
    int getX();

    int getY();

    int getZ();

    @Nullable PathPointWrapper parent();

    void setParent(@Nullable PathPointWrapper parent);
}
