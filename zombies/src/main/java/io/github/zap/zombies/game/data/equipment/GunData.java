package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.GunLevel;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Data for a gun
 */
public class GunData extends EquipmentData<GunLevel> {

    @Getter
    private final Particle particle;

    @Getter
    private final Object particleData;

    public GunData(String name, String displayName, List<String> lore, List<GunLevel> levels, Material material, Particle particle, Object particleData) {
        super(name, displayName, material, lore, levels);

        this.particle = particle;
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
