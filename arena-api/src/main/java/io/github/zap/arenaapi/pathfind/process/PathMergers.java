package io.github.zap.arenaapi.pathfind.process;

import org.jetbrains.annotations.NotNull;

public final class PathMergers {
    public static @NotNull PathMerger defaultMerger() {
        return new PathMergerImpl();
    }
}
