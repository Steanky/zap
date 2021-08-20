package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vector3I;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NodeStepper {
    @Nullable Vector3I stepDirectional(@NotNull Vector3D agentPosition, @NotNull Direction direction);
}
