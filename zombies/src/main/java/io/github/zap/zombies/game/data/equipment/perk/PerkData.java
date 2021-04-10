package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a perk
 * @param <L> The perk level type
 */
public abstract class PerkData<L extends PerkLevel> extends EquipmentData<L> {

    protected PerkData() {

    }

    @Override
    public @NotNull ItemStack createItemStack(@NotNull Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        itemStack.setAmount(level + 1);

        return itemStack;
    }

    @Override
    public @NotNull TextColor getDefaultChatColor() {
        return NamedTextColor.BLUE;
    }


    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.PERK.name();
    }

}
