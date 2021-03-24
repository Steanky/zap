package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.skill.SkillData;
import io.github.zap.zombies.game.data.equipment.skill.SkillLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents a skill
 */
public class SkillEquipment extends UpgradeableEquipment<SkillData, SkillLevel> {

    boolean usable = true;

    public SkillEquipment(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, SkillData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void onRightClick(Action action) {
        super.onRightClick(action);
        if (usable) {
            usable = false;

            final int[] timeRemaining = {getEquipmentData().getDelay()};
            ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(
                    getEquipmentData().getFormattedDisplayName(getPlayer(), getLevel()),
                    NamedTextColor.RED
            ));
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(timeRemaining[0]);
            setRepresentingItemStack(itemStack);

            getZombiesArena().runTaskTimer(20L, 20L, () -> {
                if (--timeRemaining[0] == 0) {
                    setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), getLevel()));
                    usable = true;
                } else {
                    itemStack.setAmount(timeRemaining[0]);
                }
            });
        }
    }
}
