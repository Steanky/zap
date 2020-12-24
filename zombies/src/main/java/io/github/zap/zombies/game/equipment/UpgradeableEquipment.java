package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment that can be upgraded
 * @param <T> The type of equipment data the equipment uses
 */
public class UpgradeableEquipment<T extends EquipmentData<?>> extends Equipment<T> {

    @Getter
    private int level = 0;

    public UpgradeableEquipment(Player player, int slotId, T equipmentData) {
        super(player, slotId, equipmentData);
    }

    /**
     * Upgrades the equipment
     */
    public void upgrade() {
        setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), ++level));
    }

}
