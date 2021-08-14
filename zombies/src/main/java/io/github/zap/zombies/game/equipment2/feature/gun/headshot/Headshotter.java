package io.github.zap.zombies.game.equipment2.feature.gun.headshot;

import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface Headshotter {

    boolean isHeadshot(@Nullable RayTraceResult rayTraceResult, @NotNull List<Boolean> headshotHistory);

}
