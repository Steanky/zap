package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.hotbar.HotbarObject;
import io.github.zap.zombies.game.hotbar.HotbarObjectGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Object group of a set of equipment
 */
public abstract class EquipmentObjectGroup extends HotbarObjectGroup {

    public EquipmentObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public HotbarObject createDefaultHotbarObject(Player player, int slotId) {
        Set<Integer> slots = getSlots().stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        int placeholderNumber = 1;
        for (Integer slot : slots) {
            if (slot == slotId) {
                break;
            } else {
                placeholderNumber++;
            }
        }

        return new HotbarObject(player, slotId, createPlaceholderItemStack(placeholderNumber));
    }

    /**
     * Creates the placeholder item stack
     * @param placeholderNumber The number placeholder item stack to put in the display name
     * @return The placeholder item stack
     */
    public abstract ItemStack createPlaceholderItemStack(int placeholderNumber); // TODO: this will probably need some shop stuff currently not there

    @Override
    public Integer getNextEmptySlot() {
        for (Map.Entry<Integer, HotbarObject> hotbarObjectEntry : getHotbarObjectMap().entrySet()) {
            if (!isObjectRecommendedEquipment(hotbarObjectEntry.getValue())) {
                return hotbarObjectEntry.getKey();
            }
        }

        return null;
    }

    /**
     * Checks if hotbar object is the type of equipment to store in the hotbar object group
     * @param hotbarObject The hotbar object group
     * @return Whether or not the object is the right type
     */
    public abstract boolean isObjectRecommendedEquipment(HotbarObject hotbarObject);

    @Override
    public void remove(int slotId, boolean replace) {
        super.remove(slotId, replace);
        if (replace) {
            setHotbarObject(slotId, createDefaultHotbarObject(getPlayer(), slotId));
        }
    }

    /**
     * Gets the associated equipment type of this equipment object group
     * @return The associated equipment type
     */
    public abstract EquipmentType getEquipmentType();

}
