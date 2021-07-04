package io.github.zap.nms.common.pathfind;

import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

public interface MobNavigator {
    void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed);
}
