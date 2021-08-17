package io.github.zap.zombies.game2.player.armor;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ArmorHolder {

    void setArmor(@NotNull ItemStack[] armor);

    void getArmor(@NotNull ItemStack[] dest);

}
