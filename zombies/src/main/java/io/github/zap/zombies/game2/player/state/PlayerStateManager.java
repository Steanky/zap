package io.github.zap.zombies.game2.player.state;

import org.jetbrains.annotations.NotNull;

public interface PlayerStateManager {

    void setState(@NotNull PlayerState state);

    @NotNull PlayerState getState();

}
