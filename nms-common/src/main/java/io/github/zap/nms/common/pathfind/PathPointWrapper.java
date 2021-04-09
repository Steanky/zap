package io.github.zap.nms.common.pathfind;

import org.jetbrains.annotations.Nullable;

public interface PathPointWrapper {
    int getX();

    int getY();

    int getZ();

    @Nullable PathPointWrapper parent();
}
