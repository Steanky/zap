package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

class PathAgentImpl implements PathAgent {
    private final AgentCharacteristics characteristics;

    private final double x;
    private final double y;
    private final double z;

    PathAgentImpl(@NotNull AgentCharacteristics characteristics, double x, double y, double z) {
        this.characteristics = characteristics;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull AgentCharacteristics characteristics() {
        return characteristics;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }
}
