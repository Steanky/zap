package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.hotbar.HotbarObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Object group of skills
 */
public class SkillObjectGroup extends EquipmentObjectGroup {
    public SkillObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public ItemStack createPlaceholderItemStack(int placeholderNumber) {
        return null;
    }

    @Override
    public boolean isObjectRecommendedEquipment(HotbarObject hotbarObject) {
        return hotbarObject instanceof Skill;
    }
}
