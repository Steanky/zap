package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.CustomData;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    private final String name;

    @Getter
    private final String displayName;

    @Getter
    private final Material material;

    @Getter
    private final List<String> lore;

    @Getter
    private final List<T> levels;

    public EquipmentData(String name, String displayName, Material material, List<String> lore, List<T> levels) {
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.levels = levels;

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
            itemMeta.setDisplayName(getDisplayNameChatColor() + localizationManager.getLocalizedMessage(locale, displayName));

            if (level > 0) {
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            }
            itemStack.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        } else {
            throw new IndexOutOfBoundsException(String.format("Level %d is not within the level bounds of [0, %d)!", level, levels.size()));
        }
    }

    /**
     * Gets the chat color of the display name
     * @return The chat color of the display name
     */
    public ChatColor getDisplayNameChatColor() {
        return ChatColor.RESET;
    }
}
