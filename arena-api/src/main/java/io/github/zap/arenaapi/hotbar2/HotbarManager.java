package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarManager {
    @NotNull PlayerView getOwner();

    void registerProfile(@NotNull String name, @NotNull HotbarProfile profile);

    HotbarProfile getProfile(@NotNull String name);

    void switchToProfile(@NotNull String name);

    void switchToDefaultProfile();

    @NotNull HotbarProfile currentProfile();

    void redrawCurrentProfile();
}
