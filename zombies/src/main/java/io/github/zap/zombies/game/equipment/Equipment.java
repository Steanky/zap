package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment in the hotbar
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
@Getter
public class Equipment<D extends EquipmentData<L>, L> extends HotbarObject {

    private final LocalizationManager localizationManager;

    private final D equipmentData;

    public Equipment(Player player, int slot, D equipmentData) {
        super(player, slot);

        this.localizationManager = Zombies.getInstance().getLocalizationManager();
        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(player, 0));
    }

    /**
     * Gets the current level of the equipment
     * @return The current level of the equipment
     */
    public L getCurrentLevel() {
        return equipmentData.getLevels().get(0);
    }


}
