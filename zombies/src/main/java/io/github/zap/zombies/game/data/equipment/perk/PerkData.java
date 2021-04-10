package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {

    @Getter
    private PerkType perkType;

    private PerkData() {

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
    public @NotNull String getEquipmentType() {
        return EquipmentType.PERK.name();
    }

    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.PERK.name();
    }

}
