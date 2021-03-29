package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.NavigableSet;

public interface PathResult extends Iterable<PathNode> {
    @NotNull PathNode source();

    @NotNull PathDestination destination();
}
