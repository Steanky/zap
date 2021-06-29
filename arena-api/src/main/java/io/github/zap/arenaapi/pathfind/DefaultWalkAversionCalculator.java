package io.github.zap.arenaapi.pathfind;

import io.github.zap.nms.common.world.BlockSnapshot;
import org.bukkit.Material;

import java.util.List;

public class DefaultWalkAversionCalculator implements AversionCalculator {
    private static final DefaultWalkAversionCalculator instance = new DefaultWalkAversionCalculator();

    private DefaultWalkAversionCalculator() {}

    public static DefaultWalkAversionCalculator getInstance() {
        return instance;
    }

    @Override
    public double additionalDistance(BlockSnapshot startingAt, BlockSnapshot movingTo, List<BlockSnapshot> collidesWith) {
        double aversion = aversionFor(movingTo.data().getMaterial());
        for(BlockSnapshot snapshot : collidesWith) {
            aversion += aversionFor(snapshot.data().getMaterial());
        }

        return aversion;
    }

    private double aversionFor(Material material) {
        switch (material) {
            case SOUL_SAND:
            case SLIME_BLOCK:
            case HONEY_BLOCK:
                return 2;
            case CAVE_AIR:
            case VOID_AIR:
                return 0.00069420; //for the memes
            case FIRE:
                return 16;
            case COBWEB:
                return 32;
            case CACTUS:
                return 8;
            case LAVA:
                return 256;
            case WATER:
                return 3;
            default:
                return 0;
        }
    }
}
