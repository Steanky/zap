package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

/**
 * Object group of melee weapons
 */
public class MeleeObjectGroup extends UpgradeableEquipmentObjectGroup {

    public MeleeObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public ItemStack createPlaceholderItemStack(int placeholderNumber) {
        ItemStack itemStack = new ItemStack(Material.LIGHT_GRAY_DYE);
        TextComponent name = Component.text(String.format("Weapon #%d", placeholderNumber)).color(NamedTextColor.GREEN);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public boolean isObjectRecommendedEquipment(HotbarObject hotbarObject) {
        return hotbarObject instanceof MeleeWeapon;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.MELEE.name();
    }
}
