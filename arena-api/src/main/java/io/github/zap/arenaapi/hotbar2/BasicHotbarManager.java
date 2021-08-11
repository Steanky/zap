package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

class BasicHotbarManager implements HotbarManager {
    private final PlayerView owner;
    private final Map<String, HotbarProfile> profiles = new HashMap<>();
    private final HotbarProfile defaultProfile;

    private HotbarProfile currentProfile;

    BasicHotbarManager(@NotNull PlayerView player, @NotNull HotbarProfile defaultProfile) {
        this.owner = player;
        this.defaultProfile = defaultProfile;
        switchToProfileInternal(defaultProfile); //i don't like this but it's necessary
    }

    private void switchToProfileInternal(HotbarProfile newProfile) {
        if(currentProfile != newProfile) { //if same profile, don't update
            HotbarProfile previousProfile = currentProfile;
            currentProfile = newProfile;

            if(previousProfile != null) {
                for(HotbarObject object : previousProfile) {
                    object.hide();
                }
            }

            for(HotbarObject object : currentProfile) {
                object.show();
            }
        }
    }

    @Override
    public @NotNull PlayerView getOwner() {
        return owner;
    }

    @Override
    public void registerProfile(@NotNull String name, @NotNull HotbarProfile profile) {
        profiles.put(name, profile);
    }

    @Override
    public HotbarProfile getProfile(@NotNull String name) {
        return profiles.get(name);
    }

    @Override
    public void switchToProfile(@NotNull String name) {
        HotbarProfile profile = profiles.get(name);

        if(profile != null) {
            switchToProfileInternal(profile);
        }
        else {
            throw new IllegalArgumentException("Profile named " + name + " has not been registered");
        }
    }

    @Override
    public void switchToDefaultProfile() {
        switchToProfileInternal(defaultProfile);
    }

    @Override
    public @NotNull HotbarProfile currentProfile() {
        return currentProfile;
    }
}
