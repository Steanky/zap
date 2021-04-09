package io.github.zap.nms.v1_16_R3.pathfind;

import net.minecraft.server.v1_16_R3.PathType;
import org.jetbrains.annotations.NotNull;

public class PathTypeEnumWrapper_v1_16_R3 {
    private final Enum<PathType> type;

    PathTypeEnumWrapper_v1_16_R3(@NotNull PathType from) {
        type = from;
    }
}
