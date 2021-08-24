package io.github.zap.arenaapi.hotbar2;

import io.github.zap.arenaapi.event.EventRegister;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

class BasicHotbarManager implements HotbarManager {
    private final EventRegister register;
    private final HotbarCanvas canvas;
    private final Supplier<HotbarProfile> hotbarProfileSupplier;
    private final Map<String, HotbarProfile> profiles = new HashMap<>();
    private final HotbarProfile defaultProfile;

    private HotbarProfile currentProfile;

    BasicHotbarManager(@NotNull EventRegister register, @NotNull HotbarCanvas canvas, @NotNull Supplier<HotbarProfile> hotbarProfileSupplier) {
        this.register = register;
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
        for(int i = 0; i < currentProfile.slotCapacity(); i++) {
            HotbarObject.Slotted slottedObject = currentProfile.getObject(i);
            HotbarObject hotbarObject = slottedObject.getHotbarObject();

            if(hotbarObject != null) {
                canvas.drawItem(hotbarObject.getStack(), i);
            }
            else {
                canvas.drawItem(null, i);
            }
        }
    }

    @Override
    public @NotNull HotbarCanvas getCanvas() {
        return canvas;
    }

    @Override
    public void redrawHotbarObject(int index) {
        HotbarObject.Slotted slottedObject = currentProfile.getObject(index);
        HotbarObject hotbarObject = slottedObject.getHotbarObject();

        if(hotbarObject != null) {
            canvas.drawItem(hotbarObject.getStack(), index);
        }
        else {
            canvas.drawItem(null, index);
        }
    }

    @Override
    public void redrawHotbarGroup(@NotNull String groupName) {
        HotbarObject.Slotted[] objects = currentProfile.asGroupView().getObjectsFromGroup(groupName);

        for(HotbarObject.Slotted slottedObject : objects) {
            HotbarObject hotbarObject = slottedObject.getHotbarObject();

            if(hotbarObject != null) {
                canvas.drawItem(hotbarObject.getStack(), slottedObject.getSlot());
            }
            else {
                canvas.drawItem(null, slottedObject.getSlot());
            }
        }
    }

    @Override
    public @NotNull EventRegister eventRegister() {
        return register;
    }

    @Override
    public @NotNull HotbarProfile currentProfile() {
        return currentProfile;
    }
}
