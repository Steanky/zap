package io.github.zap.zombies.data.equipment;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.data.levels.Levels;
import io.github.zap.zombies.data.CustomData;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/**
 * Data for a piece of generic equipment
 * @param <T> The type of the equipment levels
 */
public class EquipmentData<T> extends CustomData {

    @Getter
    private final LocalizationManager localizationManager;

    @Getter
    private final String name;

    @Getter
    private final String displayName;

    @Getter
    private final String description;

    @Getter
    private final String particleName;

    @Getter
    private final Object particleData;

    @Getter
    private final String materialName;

    @Getter
    private final Levels<T> levels;

    public EquipmentData(String name, String displayName, String description, String particleName, Object particleData, String materialName, Levels<T> levels) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.particleName = particleName;
        this.particleData = particleData;
        this.materialName = materialName;
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
            Material material = Material.valueOf(materialName);
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();

            Locale locale = localizationManager.getPlayerLocale(player);
            itemMeta.setDisplayName(getDisplayNameChatColor() + localizationManager.getLocalizedMessage(locale, displayName));

            if (level > 0) {
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            }
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
