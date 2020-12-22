package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.levels.GunLevel;
import io.github.zap.zombies.data.levels.Levels;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Data for a gun
 */
public class GunData extends EquipmentData<GunLevel> {
    public GunData(String name, String displayName, String description, String particleName, Object particleData, String materialName, Levels<GunLevel> levels) {
        super(name, displayName, description, particleName, particleData, materialName, levels);
    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        // TODO: some lore stuff


        return itemStack;
    }
}
