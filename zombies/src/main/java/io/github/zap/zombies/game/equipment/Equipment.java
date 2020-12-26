package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.hotbar.HotbarObject;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment in the hotbar
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
public class Equipment<D extends EquipmentData<L>, L> extends HotbarObject {

    @Getter
    private final D equipmentData;

    public Equipment(Player player, int slot, D equipmentData) {
        super(player, slot);

        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(player, 0));
    }


}
