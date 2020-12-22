package io.github.zap.zombies.equipment;

import io.github.zap.zombies.data.equipment.EquipmentData;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment that can be upgraded
 * @param <T> The type of the equipment levels
 */
public class UpgradeableEquipment<T> extends Equipment<T> {

    @Getter
    private int level = 0;

    public UpgradeableEquipment(Player player, int slotId, EquipmentData<T> equipmentData) {
        super(player, slotId, equipmentData);
    }

    /**
     * Upgrades the equipment
     */
    public void upgrade() {
        setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), ++level));
    }
}
