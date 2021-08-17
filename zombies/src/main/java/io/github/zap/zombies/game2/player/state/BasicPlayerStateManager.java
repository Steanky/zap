package io.github.zap.zombies.game2.player.state;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BasicPlayerStateManager implements PlayerStateManager {

    private final Map<PlayerState, Runnable> stateMap;

    private PlayerState state;

    public BasicPlayerStateManager(@NotNull Map<PlayerState, Runnable> stateMap, @NotNull PlayerState defaultState) {
        this.stateMap = stateMap;
        this.state = defaultState;
    }

    @Override
    public void setState(@NotNull PlayerState state) {
        this.state = state;

        Runnable runnable = stateMap.get(state);
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public @NotNull PlayerState getState() {
        return state;
    }

}
