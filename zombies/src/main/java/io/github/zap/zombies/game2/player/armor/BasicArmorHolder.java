package io.github.zap.zombies.game2.player.armor;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BasicArmorHolder implements ArmorHolder {

    private final ItemStack[] armor;

    public BasicArmorHolder(@NotNull ItemStack[] armor) {
        this.armor = armor;
    }

    @Override
    public void setArmor(@NotNull ItemStack[] armor) {
        System.arraycopy(armor, 0, this.armor, 0, armor.length);
    }

    @Override
    public void getArmor(@NotNull ItemStack[] dest) {
        System.arraycopy(this.armor, 0, dest, 0, this.armor.length);
    }

}
