package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class UpgradeableEquipmentObjectGroup extends EquipmentObjectGroup {

    private final Map<String, Integer> equipmentLevelMap = new HashMap<>();

    public UpgradeableEquipmentObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public void remove(int slot, boolean replace) {
        if (replace) {
            HotbarObject hotbarObject = getHotbarObject(slot);
            if (hotbarObject instanceof UpgradeableEquipment<?, ?>) {
                UpgradeableEquipment<?, ?> upgradeableEquipment = (UpgradeableEquipment<?, ?>) hotbarObject;
                equipmentLevelMap.put(
                        upgradeableEquipment.getEquipmentData().getName(), upgradeableEquipment.getLevel()
                );
            }
        }

        super.remove(slot, replace);
    }

    @Override
    public void setHotbarObject(int slot, HotbarObject hotbarObject) {
        if (hotbarObject instanceof UpgradeableEquipment<?, ?>) {
            UpgradeableEquipment<?, ?> upgradeableEquipment = (UpgradeableEquipment<?, ?>) hotbarObject;
            Integer level = equipmentLevelMap.get(upgradeableEquipment.getEquipmentData().getName());
            if (level != null) {
                while (upgradeableEquipment.getLevel() < level) {
                    upgradeableEquipment.upgrade();
                }
            }
        }

        super.setHotbarObject(slot, hotbarObject);
    }

}
