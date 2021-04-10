package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;

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
        if(disposed) {
            throw new ObjectDisposedException();
        }

        setEffect(getCurrentLevel());
    }

    private void setEffect(int level) {
        HotbarManager hotbarManager = getOwner().getHotbarManager();
        HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);

        while(ewSlots.size() < level) {
            // Get the next empty slot for additional gun slot
            HotbarObjectGroup gunGroup = hotbarManager.getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());
            HotbarObjectGroup defaultGroup
                    = hotbarManager.getHotbarObjectGroup(HotbarProfile.DEFAULT_HOTBAR_OBJECT_GROUP_KEY);

            Integer newSlot = defaultGroup.getNextEmptySlot();

            if (newSlot != null) {
                defaultProfile.swapSlotOwnership(newSlot, gunGroup);
                ewSlots.push(newSlot);
                break;
            }
        }

        while (ewSlots.size() > level) {
            defaultProfile.removeHotbarObject(ewSlots.pop(), false);
        }
    }

    @Override
    public void deactivate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if (isResetLevelOnDisable()) {
            setEffect(0);
        }
    }
}
