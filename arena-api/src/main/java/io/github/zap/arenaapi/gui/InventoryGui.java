package io.github.zap.arenaapi.gui;

import io.github.zap.arenaapi.ArenaApi;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * An inventory GUI with pages of item stacks
 */
public class InventoryGui implements Listener {

    private final Map<UUID, Consumer<Pair<InventoryClickEvent, Integer>>> clickHandlers = new HashMap<>();

    private final Map<UUID, Consumer<InventoryCloseEvent>> closeHandlers = new HashMap<>();

    private final List<InventoryPage> pageList = new ArrayList<>();

    private final Map<Inventory, InventoryPage> inventoryPageMap = new HashMap<>();

    private final Map<HumanEntity, Integer> playerPageNumberMap = new HashMap<>();

    public InventoryGui() {
        Bukkit.getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory != null) {
            InventoryPage inventoryPage = inventoryPageMap.get(inventory);

            if (inventoryPage != null) {
                HumanEntity player = event.getWhoClicked();
                ItemStack itemStack = inventory.getItem(event.getSlot());

                if (inventoryPage.getLeftArrow().equals(itemStack)) {
                    goToPreviousPageForPlayer(player);
                } else if (inventoryPage.getRightArrow().equals(itemStack)) {
                    goToNextPageForPlayer(player);
                } else {
                    for (Consumer<Pair<InventoryClickEvent, Integer>> handler : clickHandlers.values()) {
                        handler.accept(Pair.of(event, playerPageNumberMap.get(player)));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventoryPageMap.containsKey(event.getInventory())) {
            for (Consumer<InventoryCloseEvent> handler : closeHandlers.values()) {
                handler.accept(event);
            }

            playerPageNumberMap.remove(event.getPlayer());
        }
    }

    /**
     * Adds an inventory click handler
     * @param handler The handler to add
     * @return The UUID associated with the handler
     */
    public UUID addClickHandler(@NotNull Consumer<Pair<InventoryClickEvent, Integer>> handler) {
        UUID uuid = UUID.randomUUID();
        clickHandlers.put(uuid, handler);

        return uuid;
    }

    /**
     * Removes an inventory click handler
     * @param uuid The previously granted UUID of the click handler
     */
    public void removeClickHandler(@NotNull UUID uuid) {
        clickHandlers.remove(uuid);
    }

    /**
     * Adds an inventory close handler
     * @param handler The handler to add
     * @return The UUID associated with the handler
     */
    public UUID addCloseHandler(@NotNull Consumer<InventoryCloseEvent> handler) {
        UUID uuid = UUID.randomUUID();
        closeHandlers.put(uuid, handler);

        return uuid;
    }

    /**
     * Removes an inventory close handler
     * @param uuid The previously granted UUID of the close handler
     */
    public void removeCloseHandler(@NotNull UUID uuid) {
        closeHandlers.remove(uuid);
    }

    /**
     * Adds a page to the gui
     * @param name The name of the page
     * @param itemStacks The item stacks to put by default into the page
     * @param arrangement The arrangement of the items in the page
     * @return The new page and its index
     */
    public Pair<InventoryPage, Integer> addPage(@Nullable String name, @Nullable ItemStack[] itemStacks,
                                                @NotNull InventoryPage.Arrangement arrangement) {
        InventoryPage page = (name == null)
                ? new InventoryPage(itemStacks, arrangement)
                : new InventoryPage(name, itemStacks, arrangement);

        inventoryPageMap.put(page.getInventory(), page);
        pageList.add(page);

        return Pair.of(page, pageList.size() - 1);
    }

    /**
     * Gets a page of the inventory
     * @param pageNumber The index of the page to get
     * @return The inventory page, or null if the page number was not within bounds
     */
    public @Nullable InventoryPage getPage(int pageNumber) {
        if (0 <= pageNumber && pageNumber < inventoryPageMap.size()) {
            return pageList.get(pageNumber);
        }

        return null;
    }

    /**
     * Displays the gui to a player
     * @param player The player to display the gui to
     */
    public void displayToPlayer(@NotNull HumanEntity player) {
        if (!pageList.isEmpty()) {
            playerPageNumberMap.put(player, 0);
            displayPageToPlayer(player, 0);
        }
    }

    /**
     * Flips the player to the previous page
     * @param player The player to display the previous page to
     */
    public void goToPreviousPageForPlayer(@NotNull HumanEntity player) {
        int pageNumber = playerPageNumberMap.get(player);

        if (pageNumber > 0) {
            playerPageNumberMap.put(player, pageNumber - 1);
            displayPageToPlayer(player, pageNumber - 1);
        }
    }

    /**
     * Flips the player to the next page
     * @param player The player to display the previous page to
     */
    public void goToNextPageForPlayer(@NotNull HumanEntity player) {
        int pageNumber = playerPageNumberMap.get(player);

        if (pageNumber < pageList.size() - 1) {
            playerPageNumberMap.put(player, pageNumber + 1);
            displayPageToPlayer(player, pageNumber + 1);
        }
    }

    /**
     * Displays a page of an inventory to a player
     * @param player The player to display the page to
     * @param pageNumber The index of the page to display
     */
    public void displayPageToPlayer(@NotNull HumanEntity player, int pageNumber) {
        if (0 <= pageNumber && pageNumber < pageList.size()) {
            player.openInventory(pageList.get(pageNumber).getInventory());
        }
    }

}
