package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.hotbar.HotbarObject;
import io.github.zap.zombies.game.hotbar.HotbarObjectGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GunObjectGroup extends HotbarObjectGroup {

    public GunObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public HotbarObject createDefaultHotbarObject(Player player, int slotId) {
        Set<Integer> slots = getSlots().stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        int number = 1;
        for (Integer slot : slots) {
            if (slot == slotId) {
                break;
            } else {
                number++;
            }
        }

        LocalizationManager localizationManager = Zombies.getInstance().getLocalizationManager();
        Locale locale = localizationManager.getPlayerLocale(player);

        ItemStack itemStack = new ItemStack(Material.LIGHT_GRAY_DYE);
        ItemMeta itemMeta = itemStack.getItemMeta();

        // TODO: getting the locations of shops where you can buy

        itemStack.setItemMeta(itemMeta);

        return new HotbarObject(player, slotId, itemStack);
    }

    @Override
    public Integer getNextEmptySlot() {
        for (Map.Entry<Integer, HotbarObject> hotbarObjectEntry : getHotbarObjectMap().entrySet()) {
            if (!(hotbarObjectEntry.getValue() instanceof Gun<?>)) {
                return hotbarObjectEntry.getKey();
            }
        }

        return null;
    }

    @Override
    public void remove(int slotId, boolean replace) {
        super.remove(slotId, replace);
        if (replace) {
            setHotbarObject(slotId, createDefaultHotbarObject(getPlayer(), slotId));
        }
    }
}
