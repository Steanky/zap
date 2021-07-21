package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

record PathAgentImpl(AgentCharacteristics characteristics, double x, double y, double z) implements PathAgent {
    PathAgentImpl(@NotNull AgentCharacteristics characteristics, double x, double y, double z) {
        this.characteristics = characteristics;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull
    AgentCharacteristics characteristics() {
        return characteristics;
    }
}
