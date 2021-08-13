package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

class BasicHotbarManager implements HotbarManager {
    private final HotbarCanvas canvas;
    private final Supplier<HotbarProfile> hotbarProfileSupplier;
    private final Map<String, HotbarProfile> profiles = new HashMap<>();
    private final HotbarProfile defaultProfile;

    private HotbarProfile currentProfile;

    BasicHotbarManager(@NotNull HotbarCanvas canvas, @NotNull Supplier<HotbarProfile> hotbarProfileSupplier) {
        this.canvas = canvas;
        this.hotbarProfileSupplier = hotbarProfileSupplier;
        this.currentProfile = this.defaultProfile = hotbarProfileSupplier.get();
    }

    @Override
    public void newProfile(@NotNull String name) {
        profiles.put(name, Objects.requireNonNull(hotbarProfileSupplier.get(), "HotbarProfile was null"));
    }

    @Override
    public HotbarProfile getProfile(@NotNull String name) {
        return profiles.get(name);
    }

    @Override
    public @NotNull HotbarCanvas getCanvas() {
        return canvas;
    }

    @Override
    public void switchToProfile(@NotNull String name) {
        HotbarProfile profile = profiles.get(name);

        if(profile != null) {
            currentProfile = profile;
        }
        else {
            throw new IllegalArgumentException("Profile named " + name + " has not been registered");
        }
    }

    @Override
    public void switchToDefaultProfile() {
        currentProfile = defaultProfile;
    }

    @Override
    public void redrawCurrentProfile() {
        for(int i = 0; i < 9; i++) {
            HotbarObject hotbarObject = currentProfile.getObject(i);

            if(hotbarObject != null) {
                canvas.drawItem(hotbarObject.getStack(), i);
            }
            else {
                canvas.drawItem(null, i);
            }
        }
    }

    @Override
    public void redrawHotbarObject(int index) {
        HotbarObject hotbarObject = currentProfile.getObject(index);

        if(hotbarObject != null) {
            canvas.drawItem(hotbarObject.getStack(), index);
        }
        else {
            canvas.drawItem(null, index);
        }
    }

    @Override
    public void redrawHotbarObject(@NotNull HotbarObject object) {
        int index = currentProfile.indexOf(object);

        if(index != -1) {
            canvas.drawItem(object.getStack(), index);
        }
        else {
            throw new IllegalArgumentException("HotbarObject " + object + " is not managed by the current profile");
        }
    }

    @Override
    public @NotNull HotbarProfile currentProfile() {
        return currentProfile;
    }
}
