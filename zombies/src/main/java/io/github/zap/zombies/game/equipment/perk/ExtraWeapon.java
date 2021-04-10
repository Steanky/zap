package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.perk.ExtraWeaponData;
import io.github.zap.zombies.game.data.equipment.perk.ExtraWeaponLevel;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Allows players to have extra weapon slots
 */
public class ExtraWeapon extends MarkerPerkEquipment<ExtraWeaponData, ExtraWeaponLevel> {

    private int currentLevel = 0; // local variable since we cannot reliably get the current level

    // TODO: allow for extra weapons to grant whatever slot type
    public ExtraWeapon(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                       @NotNull ExtraWeaponData perkData) {
        super(arena, player, slot, perkData);
    }

    private void setLevel(int level) {
        HotbarManager hotbarManager = getZombiesPlayer().getHotbarManager();
        HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);

        while (currentLevel < level) {
            Set<Integer> removeSlots = getEquipmentData().getLevels().get(currentLevel++).getNewSlots();

            HotbarObjectGroup gunGroup = hotbarManager.getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());

            for (Integer slot : removeSlots) {
                defaultProfile.swapSlotOwnership(slot, gunGroup);
            }
        }

        while (currentLevel > level) {
            Set<Integer> removeSlots = getEquipmentData().getLevels().get(currentLevel--).getNewSlots();
            for (Integer slot : removeSlots) {
                defaultProfile.removeHotbarObject(slot, false);
            }
        }
    }

    @Override
    public void activate() {
        setLevel(getLevel());
    }

    @Override
    public void deactivate() {
        setLevel(0);
    }

}
