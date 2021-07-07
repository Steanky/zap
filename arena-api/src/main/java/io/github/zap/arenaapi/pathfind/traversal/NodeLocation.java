package io.github.zap.arenaapi.pathfind.traversal;

import org.jetbrains.annotations.NotNull;

public record NodeLocation(@NotNull NodeRow parent, @NotNull Object node, int parentIndex) { }
