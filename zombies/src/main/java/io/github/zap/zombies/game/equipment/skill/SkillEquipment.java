package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.skill.SkillData;
import io.github.zap.zombies.game.data.equipment.skill.SkillLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a skill
 */
public class SkillEquipment extends UpgradeableEquipment<SkillData, SkillLevel> {

    boolean usable = true;

    public SkillEquipment(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, SkillData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void onRightClick() {
        super.onRightClick();
        if (usable) {
            usable = false;

            final int[] timeRemaining = {getEquipmentData().getDelay()};
            ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(getEquipmentData().getFormattedDisplayNameWithChatColor(ChatColor.RED, getPlayer(),
                    getLevel()));
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(timeRemaining[0]);
            setRepresentingItemStack(itemStack);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (--timeRemaining[0] == 0) {
                        setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), getLevel()));
                        usable = true;
                    } else {
                        itemStack.setAmount(timeRemaining[0]);
                    }
                }
            }.runTaskTimer(Zombies.getInstance(), 20L, 20L);
        }
    }
}
