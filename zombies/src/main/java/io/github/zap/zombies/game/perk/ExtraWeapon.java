package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.zombies.game.ZombiesPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ExtraWeapon extends MarkerPerk {
    public ExtraWeapon(ZombiesPlayer owner, int maxLevel, boolean resetOnQuit) {
        super(owner, maxLevel, resetOnQuit);
    }

    private final Stack<Integer> ewSlots = new Stack<>();

    // don't return Stack directly to avoid caller accidentally mutate our ewSlots
    public List<Integer> getSlots() {
        return Collections.unmodifiableList(ewSlots);
    }

    @Override
    public void activate() {
        while(ewSlots.size() < getCurrentLevel()) {
            // Get the next empty slot for additional gun slot
            var gunGroup = getOwner().getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            var newSlot = gunGroup.getNextEmptySlot();
            var defaultProfile = getOwner().getHotbarManager().getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);
            defaultProfile.swapSlotOwnership(newSlot, gunGroup);
            ewSlots.push(newSlot);
        }

        while (ewSlots.size() > getCurrentLevel()) {
            var gunGroup = getOwner().getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            gunGroup.remove(ewSlots.pop(), false);
        }
    }

    //TODO implement deactivation code
    @Override
    public void deactivate() {

    }
}
