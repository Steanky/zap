package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.equipment.EquipmentType;

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
        setEffect(getCurrentLevel());
    }

    private void setEffect(int level) {
        while(ewSlots.size() < level) {
            // Get the next empty slot for additional gun slot
            HotbarManager hotbarManager = getOwner().getHotbarManager();
            HotbarObjectGroup gunGroup = hotbarManager.getHotbarObjectGroup(EquipmentType.GUN.name());
            HotbarObjectGroup defaultGroup
                    = hotbarManager.getHotbarObjectGroup(HotbarProfile.DEFAULT_HOTBAR_OBJECT_GROUP_KEY);

            HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);
            Integer newSlot = defaultGroup.getNextEmptySlot();

            if (newSlot != null) {
                defaultProfile.swapSlotOwnership(newSlot, gunGroup);
                ewSlots.push(newSlot);
                break;
            }
        }

        while (ewSlots.size() > level) {
            HotbarObjectGroup gunGroup = getOwner().getHotbarManager().getHotbarObjectGroup(EquipmentType.GUN.name());
            gunGroup.remove(ewSlots.pop(), false);
        }
    }

    @Override
    public void deactivate() {
        setEffect(0);
    }
}
