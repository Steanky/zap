package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.CustomData;
import io.github.zap.zombies.game.data.util.RomanNumeral;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data for a piece of generic equipment
 * @param <T> The type of the equipment levels
 */
public abstract class EquipmentData<T> extends CustomData {

    @Getter
    private transient final LocalizationManager localizationManager;

    @Getter
    private String displayName;

    @Getter
    private Material material;

    @Getter
    private List<String> lore;

    @Getter
    private List<T> levels;

    public EquipmentData(String displayName, Material material, List<String> lore, List<T> levels) {
        this();
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.levels = levels;
    }

    protected EquipmentData() {
        localizationManager = Zombies.getInstance().getLocalizationManager();
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

            Locale locale = localizationManager.getPlayerLocale(player);
            itemMeta.setDisplayName(getFormattedDisplayName(level, localizationManager.getLocalizedMessage(locale, displayName)));

            if (level > 0) {
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            }
            itemMeta.setLore(getLore(player, level));
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        } else {
            throw new IndexOutOfBoundsException(String.format("Level %d is not within the level bounds of [0, %d)!", level, levels.size()));
        }
    }

    public List<String> getLore(Player player, int level) {
        List<String> lore = new ArrayList<>(getLore());
        lore.set(0, ChatColor.RESET.toString() + ChatColor.GRAY.toString() + lore.get(0));

        return lore;
    }

    /**
     * Gets the formatted version of the display name with any add-ons
     * @return The formatted version of the display name
     */
    public String getFormattedDisplayName(int level, String displayName) {
        if (level > 0) {
            displayName = ChatColor.BOLD.toString() + displayName;
            displayName += " Ultimate";

            if (level > 1) {
                displayName += " " + RomanNumeral.toRoman(level);
            }
        }

        return displayName;
    }

}
