package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
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

    private final ZombiesArena zombiesArena;

    private final ZombiesPlayer zombiesPlayer;

    private final LocalizationManager localizationManager;

    private final D equipmentData;

    public Equipment(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, D equipmentData) {
        super(zombiesPlayer.getPlayer(), slot);

        this.zombiesArena = zombiesArena;
        this.zombiesPlayer = zombiesPlayer;
        this.localizationManager = Zombies.getInstance().getLocalizationManager();
        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(zombiesPlayer.getPlayer(), 0));
    }

    /**
     * Gets the current level of the equipment
     * @return The current level of the equipment
     */
    public L getCurrentLevel() {
        return equipmentData.getLevels().get(0);
    }


}
