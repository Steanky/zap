package io.github.zap.zombies.game.equipment2.feature.gun.headshot;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicHeadshotter implements Headshotter {

    private final boolean inverted;

    private final boolean defaultHeadshot;

    public BasicHeadshotter(boolean inverted, boolean defaultHeadshot) {
        this.inverted = inverted;
        this.defaultHeadshot = defaultHeadshot;
    }

    @Override
    public boolean isHeadshot(@Nullable RayTraceResult rayTraceResult, @NotNull List<Boolean> headshotHistory) {
        if (rayTraceResult != null) {
            Entity hit = rayTraceResult.getHitEntity();
            if (hit != null) {
                if (hit instanceof LivingEntity livingEntity) {
                    // height - 2 * (height - eyeHeight) = 2 * eyeHeight - height
                    double bottomY = 2 * livingEntity.getEyeHeight() - livingEntity.getHeight();
                    double hitY = rayTraceResult.getHitPosition().getY();

                    boolean headshot = bottomY <= hitY && hitY <= livingEntity.getHeight();

                    /*
                     * inverted, headshot -> false
                     * inverted, !headshot -> true
                     * !inverted, headshot -> true
                     * !inverted, !headshot -> false
                     */
                    return headshot != inverted;
                } else throw new IllegalArgumentException("Tried to determine a headshot on a raytrace that did not " +
                        "involve a living entity!");
            } else
                throw new IllegalArgumentException("Tried to determine a headshot on a raytrace that did not involve " +
                        "a living entity!");
        }

        return defaultHeadshot;
    }

}
