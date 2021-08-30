package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;


public interface AversionCalculator {
    double aversionForMaterial(@NotNull Material material);

    double aversionFactor(@NotNull PathNode linkedNode);
}
