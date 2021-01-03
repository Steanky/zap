package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A piece of equipment that can be upgraded
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
public class UpgradeableEquipment<D extends EquipmentData<L>, L> extends Equipment<D, L> {

    @Getter
    private int level = 0;

    public UpgradeableEquipment(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, D equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    /**
     * Upgrades the equipment
     */
    public void upgrade() {
        setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), ++level));
    }

    @Override
    public L getCurrentLevel() {
        return getEquipmentData().getLevels().get(level);
    }
}
