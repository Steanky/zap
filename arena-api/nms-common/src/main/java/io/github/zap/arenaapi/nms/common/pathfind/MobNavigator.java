package io.github.zap.arenaapi.nms.common.pathfind;

import org.jetbrains.annotations.NotNull;

public interface MobNavigator {
    void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed);
}
