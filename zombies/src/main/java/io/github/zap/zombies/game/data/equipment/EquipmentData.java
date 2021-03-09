package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.util.RomanNumeral;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a piece of generic equipment
 * @param <L> The type of the equipment levels
 */
@Getter
public abstract class EquipmentData<L> {

    private String type;

    private String name;

    private String displayName;

    private Material material;

    private List<String> lore;

    private List<L> levels;

    public EquipmentData(String type, String name, String displayName, List<String> lore, List<L> levels, Material material) {
        this.type = type;
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.levels = levels;
    }

    protected EquipmentData() {

    }

    /**
     * Creates an item stack that represents the equipment
     * @param player The player to create the item stack for and to get the locale from
     * @param level The level of the equipment
     * @return An item stack representing the equipment
     */
    public ItemStack createItemStack(Player player, int level) {
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

    public List<String> getLore(Player player, int level) {
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
    public String getFormattedDisplayName(Player player, int level) {
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
     * Get the default chat color of the equipment
     * @return The default chat color of the equipment
     */
    public abstract TextColor getDefaultChatColor();

    /**
     * Gets the string representation of the type of the equipment
     * @return The type of the equipment
     */
    public String getEquipmentType() {
        return type;
    }

}
