package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.TeamMachineData;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamMachine extends BlockShop<TeamMachineData> {

    private final Inventory inventory;

    private final Map<Integer, TeamMachineTask> slotMap = new HashMap<>();

    public TeamMachine(ZombiesArena zombiesArena, TeamMachineData shopData) {
        super(zombiesArena, shopData);

        this.inventory = prepareInventory();
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();

        ZombiesArena zombiesArena = getZombiesArena();
        zombiesArena.getInventoryClickEvent().registerHandler(args -> {
            InventoryClickEvent inventoryClickEvent = args.getEvent();

            if (inventory.equals(inventoryClickEvent.getClickedInventory())) {
                ZombiesPlayer zombiesPlayer = zombiesArena.getPlayerMap()
                        .get(inventoryClickEvent.getWhoClicked().getUniqueId());

                if (zombiesPlayer != null) {
                    TeamMachineTask teamMachineTask = slotMap.get(inventoryClickEvent.getSlot());

                    if (teamMachineTask != null && teamMachineTask.execute(zombiesArena, zombiesPlayer)) {
                        inventoryClickEvent.setCancelled(true);
                        onPurchaseSuccess(zombiesPlayer);
                    }
                }
            }
        });
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();

        hologram.setLine(0, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Team Machine");

        hologram.setLine(1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!"
                        : ChatColor.GREEN + "rc to open"
        );
    }

    @Override
    protected boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            args.getManagedPlayer().getPlayer().openInventory(inventory);
        }

        return false;
    }

    private Inventory prepareInventory() {
        List<TeamMachineTask> teamMachineTasks = getShopData().getTeamMachineTasks();

        int num = teamMachineTasks.size();
        int width = (int) Math.ceil(Math.sqrt(num));
        int height = (int) Math.ceil((double) num / width);
        int remainderLine = Math.min(6, height) / 2;
        // this is the first line offset
        int offset = (height <= 4) ? 1 : 0;
        // If the height go higher than 6 we need to change our calculation
        if (height > 6) {
            width = (int) Math.ceil((double) num / 6);
        }
        int finalLine = num % width;
        if (finalLine == 0) {
            finalLine = width;
        }

        int guiSize = 9 * Math.min(6, height + 2);
        Inventory inventory = Bukkit.createInventory(null, guiSize, "Team Machine");

        int index = 0;

        for (int h = 0; h < height; h++) {
            int lineCount = (h == remainderLine) ? finalLine : width;
            for (int w = 0; w < lineCount && index < num; w++) {
                int slot = (18 * w + 9) / (2 * lineCount);
                int pos = (h + offset) * 9 + slot;

                TeamMachineTask teamMachineTask = teamMachineTasks.get(index);
                inventory.setItem(pos, new ItemStack(teamMachineTask.getDisplayMaterial()));
                slotMap.put(pos, teamMachineTask);

                index++;
            }
        }

        return inventory;
    }

    @Override
    public String getShopType() {
        return ShopType.TEAM_MACHINE.name();
    }
}
