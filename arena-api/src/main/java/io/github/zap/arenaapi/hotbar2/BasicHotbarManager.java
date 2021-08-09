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
    }

    private void switchProfileInternal(HotbarProfile newProfile) {
        if(currentProfile != newProfile) { //if same profile, don't update
            HotbarProfile previousProfile = currentProfile;
            currentProfile = newProfile;

            for(int i = 0; i < 9; i++) {
                HotbarObject previousObject = previousProfile == null ? null : previousProfile.getObject(i);
                HotbarObject currentObject = currentProfile.getObject(i);

                if(previousObject != null) {
                    previousObject.onDeactivate();
                }

                if(currentObject != null) {
                    currentObject.onActivate();
                }
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
            switchProfileInternal(profile);
        }
        else {
            throw new IllegalArgumentException("Profile named " + name + " has not been registered");
        }
    }

    @Override
    public void switchToDefaultProfile() {
        switchProfileInternal(defaultProfile);
    }

    @Override
    public HotbarProfile currentProfile() {
        return currentProfile;
    }
}
