package io.github.zap.arenaapi.gui;

import io.github.zap.arenaapi.Unique;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a page of an inventory GUI
 */
public class InventoryPage implements Unique {

    private final UUID id = UUID.randomUUID();

    @Getter(value = AccessLevel.PACKAGE)
    private final Inventory inventory;

    private final Map<Integer, ItemStack> arrangedSlotMap = new HashMap<>();

    private final Map<Integer, ItemStack> unArrangedSlotMap = new HashMap<>();

    @Getter(value = AccessLevel.PACKAGE)
    private final ItemStack leftArrow = new ItemStack(Material.ARROW), rightArrow = new ItemStack(Material.ARROW);

    @Getter
    private Arrangement arrangement;

    public InventoryPage(@NotNull String name, @Nullable ItemStack[] itemStacks, @NotNull Arrangement arrangement) {
        if (itemStacks != null && itemStacks.length > 52) {
            throw new IllegalArgumentException("InventoryPages can only have up to 52 items!");
        }

        inventory = Bukkit.createInventory(null, 54, Component.text(name));

        unArrangedSlotMap.put(45, leftArrow);
        unArrangedSlotMap.put(53, rightArrow);
        arrangeSlotMap(itemStacks, arrangement);
    }

    public InventoryPage(@Nullable ItemStack[] itemStacks, @NotNull Arrangement arrangement) {
        if (itemStacks != null && itemStacks.length > 52) {
            throw new IllegalArgumentException("InventoryPages can only have up to 52 items!");
        }

        inventory = Bukkit.createInventory(null, 54);

        unArrangedSlotMap.put(45, leftArrow);
        unArrangedSlotMap.put(53, rightArrow);
        arrangeSlotMap(itemStacks, arrangement);
    }

    private void arrangeSlotMap(ItemStack[] itemStacks, Arrangement arrangement) {
        this.arrangement = arrangement;

        switch (arrangement) {
            case IN_ORDER:
                arrangeInOrder(itemStacks);
                break;
            case DISTRIBUTED:
                arrangeDistributed(itemStacks);
                break;
        }

        for (Map.Entry<Integer, ItemStack> unarrangedSlotEntry : unArrangedSlotMap.entrySet()) {
            inventory.setItem(unarrangedSlotEntry.getKey(), unarrangedSlotEntry.getValue());
        }
    }

    private void arrangeInOrder(ItemStack[] itemStacks) {
        int bound = Math.min(itemStacks.length, 45);
        for (int i = 0; i < bound; i++) {
            ItemStack itemStack = itemStacks[i];

            arrangedSlotMap.put(i, itemStack);
            inventory.setItem(i, itemStack);
        }

        bound = Math.min(itemStacks.length, 52);
        for (int i = 45; i < bound; i++) {
            ItemStack itemStack = itemStacks[i];
            arrangedSlotMap.put(i + 1, itemStack);
            inventory.setItem(i + 1, itemStack);
        }
    }

    private void arrangeDistributed(ItemStack[] itemStacks) {
        throw new NotImplementedException(); // TODO: figure out an algorithm
    }

    /**
     * Rearranges the items in the page
     * @param arrangement The new arrangement of the items
     * @param newItemStacks Any new item stacks that should be added on in the arrangement
     */
    public void setArrangement(@NotNull Arrangement arrangement, @NotNull ItemStack... newItemStacks) {
        if (this.arrangement != arrangement || newItemStacks.length > 0) {
            Collection<ItemStack> itemStackCollection = arrangedSlotMap.values();
            itemStackCollection.addAll(Arrays.asList(newItemStacks));

            ItemStack[] itemStacks = itemStackCollection.toArray(new ItemStack[0]);

            arrangedSlotMap.clear();
            inventory.clear();

            arrangeSlotMap(itemStacks, arrangement);
        }
    }

    /**
     * Sets the item stack at a slot which will not be arranged by the page's arrangement
     * @param slot The slot to set the item stack at
     * @param itemStack The item stack
     */
    public void setItemStackAt(int slot, @NotNull ItemStack itemStack) {
        if (slot != 45 && slot != 53) {
            unArrangedSlotMap.put(slot, itemStack);
            inventory.setItem(slot, itemStack);
        }
    }

    /**
     * Adds item stacks into the page
     * @param itemStacks The item stacks to add
     */
    public void addItemStacks(@NotNull ItemStack... itemStacks) {
        setArrangement(arrangement, itemStacks);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryPage that = (InventoryPage) o;
        return that.getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum Arrangement {
        IN_ORDER,
        DISTRIBUTED
    }

}
