package io.github.zap.arenaapi.pathfind;

import net.minecraft.server.v1_16_R3.PathEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.NavigableSet;

public interface PathResult {
    @NotNull PathNode source();

    @NotNull PathDestination destination();

    @NotNull PathEntity toPathEntity();
}
