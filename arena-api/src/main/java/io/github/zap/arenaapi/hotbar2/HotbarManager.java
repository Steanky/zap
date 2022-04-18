package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface HotbarManager {
    void newProfile(@NotNull String name);

    HotbarProfile getProfile(@NotNull String name);

    @NotNull HotbarCanvas getCanvas();

    void switchToProfile(@NotNull String name);

    void switchToDefaultProfile();

    @NotNull HotbarProfile currentProfile();

    void redrawCurrentProfile();

    void redrawHotbarObject(int index);

    void redrawHotbarGroup(@NotNull String groupName);

    static @NotNull HotbarManager newManager(@NotNull HotbarCanvas canvas, @NotNull Supplier<HotbarProfile> profileSupplier) {
        return new BasicHotbarManager(canvas, profileSupplier);
    }
}
