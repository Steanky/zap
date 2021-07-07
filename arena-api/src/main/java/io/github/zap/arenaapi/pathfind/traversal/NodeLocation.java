package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import org.jetbrains.annotations.NotNull;

public record NodeLocation(@NotNull NodeRow parent, @NotNull PathNode node, int parentIndex) { }
