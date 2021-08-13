package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarManager {
    void newProfile(@NotNull String name);

    HotbarProfile getProfile(@NotNull String name);

    @NotNull HotbarCanvas getCanvas();

    void switchToProfile(@NotNull String name);

    void switchToDefaultProfile();

    @NotNull HotbarProfile currentProfile();

    void redrawCurrentProfile();

    void redrawHotbarObject(int index);

    void redrawHotbarObject(@NotNull HotbarObject object);
}
