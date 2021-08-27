package io.github.zap.arenaapi.pathfind.step;

import org.jetbrains.annotations.NotNull;

public final class NodeSteppers {
    public static @NotNull NodeStepper basicWalk() {
        return new WalkNodeStepper();
    }
}
