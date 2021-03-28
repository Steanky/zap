package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

public interface PathResult {
    @NotNull PathNode source();

    @NotNull PathNode destination();

    @NotNull LinkedHashSet<PathNode> nodes();

    @NotNull PathEntity toPathEntity();
}
