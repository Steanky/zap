package io.github.zap.arenaapi.pathfind.calculate;

import io.github.zap.arenaapi.pathfind.path.PathNode;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public final class AversionCalculators {
    public static @NotNull AversionCalculator defaultWalk() {
        return new AversionCalculator() {
            @Override
            public double aversionForMaterial(@NotNull Material material) {
                return switch (material) {
                    case SOUL_SAND, SLIME_BLOCK, HONEY_BLOCK -> 4;
                    case FIRE -> 8;
                    case COBWEB -> 16;
                    case CACTUS, SWEET_BERRY_BUSH -> 12;
                    case LAVA -> 256;
                    case WATER -> 3;
                    default -> 0;
                };
            }

            @Override
            public double aversionFactor(@NotNull PathNode linkedNode) {
                PathNode parent = linkedNode.parent();
                if(parent != null) {
                    return parent.y() - linkedNode.y() <= 3 ? 1 : 2;
                }

                return 1;
            }
        };
    }
}
