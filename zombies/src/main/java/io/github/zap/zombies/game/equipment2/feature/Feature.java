package io.github.zap.zombies.game.equipment2.feature;

import io.github.zap.zombies.game.equipment2.Equipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Feature {

    @Nullable ItemStack getVisual(@NotNull Equipment equipment);

}
