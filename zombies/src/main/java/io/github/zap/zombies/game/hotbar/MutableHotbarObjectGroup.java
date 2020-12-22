package io.github.zap.zombies.game.hotbar;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

/**
 * Represents a hotbar object group that can grow to add more slots
 * This should in theory be used only by the default hotbar object group in {@link HotbarProfile}
 */
public class MutableHotbarObjectGroup extends HotbarObjectGroup {

    private final Player player;

    public MutableHotbarObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);

        this.player = player;
    }

    /**
     * Adds a new empty hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slotId The slot to add the empty hotbar object to
     */
    public void addObject(int slotId) {
        addObject(slotId, new HotbarObject(player, slotId));
    }

    /**
     * Adds a new hotbar object to a new slot.
     * This should not be used to set hotbar objects
     * @param slotId The slot to add the hotbar object to
     * @param hotbarObject The hotbar object to add
     */
    public void addObject(int slotId, HotbarObject hotbarObject) {
        Map<Integer, HotbarObject> hotbarObjectMap = getHotbarObjectMap();
        if (!hotbarObjectMap.containsKey(slotId)) {
            hotbarObjectMap.put(slotId, hotbarObject);
            hotbarObject.setVisible(isVisible());
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup already contains slotId %d! (Did you mean to use setHotbarObject?)", slotId));
        }
    }

}
