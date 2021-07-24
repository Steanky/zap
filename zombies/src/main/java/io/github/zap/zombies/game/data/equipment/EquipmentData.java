package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.util.RomanNumeral;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a piece of generic equipment
 * @param <L> The type of the equipment levels
 */
public abstract class EquipmentData<L extends @NotNull Object> {

    private String type;

    private String name;

    private String displayName;

    private Material material;

    private List<String> lore;

    private List<L> levels;

    protected EquipmentData() {

    }

    public EquipmentData(@NotNull String type, @NotNull String name, @NotNull String displayName,
                         @NotNull Material material, @NotNull List<String> lore, @NotNull List<L> levels) {
        this.type = type;
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.levels = levels;
    }

    /**
     * Creates an item stack that represents the equipment
     * @param player The player to create the item stack for and to get the locale from
     * @param level The level of the equipment
     * @return An item stack representing the equipment
     */
    public @NotNull ItemStack createItemStack(@NotNull OfflinePlayer player, int level) {
        if (0 <= level && level < levels.size()) {
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.displayName(Component.text(getFormattedDisplayName(player, level), getDefaultChatColor()));

            itemMeta.setLore(getLore(player, level));
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        } else {
            throw new IndexOutOfBoundsException(String.format("Level %d is not within the level bounds of [0, %d)!",
                    level, levels.size()));
        }
    }

    public @NotNull List<String> getLore(@NotNull OfflinePlayer player, int level) {
        List<String> lore = new ArrayList<>(getLore());
        lore.set(0, ChatColor.RESET.toString() + ChatColor.GRAY.toString() + lore.get(0));

        return lore;
    }

    /**
     * Gets the formatted version of the display name
     * @param player The player for which the equipment is given to
     * @param level The level of the equipment display
     * @return The formatted version of the display name
     */
    public @NotNull String getFormattedDisplayName(@NotNull OfflinePlayer player, int level) {
        String formattedDisplayName = displayName;
        if (level > 0) {
            formattedDisplayName = String.format(
                    "%s%s %s",
                    ChatColor.BOLD.toString(),
                    formattedDisplayName,
                    RomanNumeral.toRoman(level + 1)
            );
        }

        return formattedDisplayName;
    }

    /**
     * Gets the default chat color of the equipment
     * @return The default chat color of the equipment
     */
    public abstract @NotNull TextColor getDefaultChatColor();
    /**
     * Gets the equipment object group type of the equipment as a string
     * @return The equipment object group type of the equipment
     */
    public abstract @NotNull String getEquipmentObjectGroupType();

    /**
     * Gets the type of the equipment
     * @return The type as a string
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Gets the internal name of the equipment
     * @return The internal name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets the display name of the equipment
     * @return The display name
     */
    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the material used for the equipment
     * @return The material
     */
    public @NotNull Material getMaterial() {
        return material;
    }

    /**
     * Gets the lore that is part of the equipment
     * @return The lore
     */
    public @NotNull List<String> getLore() {
        return lore;
    }

    /**
     * Gets the levels of the equipment
     * @return The levels
     */
    public @NotNull List<L> getLevels() {
        return levels;
    }

}
