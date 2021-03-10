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

    private Stack<Integer> ewSlots = new Stack<>();

    // don't return Stack directly to avoid caller accidentally mutate our ewSlots
    public List<Integer> getSlots() {
        return Collections.unmodifiableList(ewSlots);
    }

    @Override
    public void activate() {
        super.activate();
        setEffect(getCurrentLevel());
    }

    private void setEffect(int level) {
        while(ewSlots.size() < level) {
            // Get the next empty slot for additional gun slot
            var gunGroup = getOwner().getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            var newSlot = gunGroup.getNextEmptySlot();
            var defaultProfile = getOwner().getHotbarManager().getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);
            defaultProfile.swapSlotOwnership(newSlot, gunGroup);
            ewSlots.push(newSlot);
        }

        while (ewSlots.size() > level) {
            var gunGroup = getOwner().getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            gunGroup.remove(ewSlots.pop(), false);
        }
    }

    @Override
    public void disable() {
        super.disable();
        setEffect(0);
    }
}
