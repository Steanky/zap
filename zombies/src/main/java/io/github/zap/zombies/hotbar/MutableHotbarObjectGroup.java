package io.github.zap.zombies.hotbar;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Represents a hotbar object group that can grow to add more slots
 * This should in theory be used only by the default hotbar object group in {@link HotbarProfile}
 */
public class MutableHotbarObjectGroup extends HotbarObjectGroup {

    private final Player player;

    private boolean visible;

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
        if (!hotbarObjectMap.containsKey(slotId)) {
            hotbarObjectMap.put(slotId, hotbarObject);
            hotbarObject.setVisible(visible);
        } else {
            throw new IllegalArgumentException(String.format("The HotbarObjectGroup already contains slotId %d! (Did you mean to use setHotbarObject?)", slotId));
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible;
    }
}
