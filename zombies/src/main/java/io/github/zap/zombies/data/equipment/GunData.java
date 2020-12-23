package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.levels.GunLevel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Data for a gun
 */
public class GunData extends EquipmentData<GunLevel> {

    @Getter
    private final String particleName;

    @Getter
    private final Object particleData;

    public GunData(String name, String displayName, List<String> lore, String particleName, Object particleData, String materialName, List<GunLevel> levels) {
        super(name, displayName, lore, particleName, particleData, materialName, levels);

        this.particleName = particleName;
        this.particleData = particleData;
    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        // TODO: some lore stuff

        GunLevel gunLevel = getLevels().get(level);

        return itemStack;
    }
}
