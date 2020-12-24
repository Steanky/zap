package io.github.zap.zombies.game.data.equipment.gun;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.level.GunLevel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Data for a gun
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LinearGunData.class, name = "linear")
})
public class GunData extends EquipmentData<GunLevel> {

    private String type;

    public GunData(String type, String name, String displayName, List<String> lore, List<GunLevel> levels, Material material) {
        super(name, displayName, material, lore, levels);
        this.type = type;
    }

    protected GunData() {

    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        // TODO: some lore stuff

        GunLevel gunLevel = getLevels().get(level);

        return itemStack;
    }
}
