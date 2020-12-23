package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.hotbar.HotbarObject;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment in the hotbar
 * @param <T> The type of the equipment levels
 */
public class Equipment<T> extends HotbarObject {

    @Getter
    private final EquipmentData<T> equipmentData;

    public Equipment(Player player, int slotId, EquipmentData<T> equipmentData) {
        super(player, slotId);

        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(player, 0));
    }


}
