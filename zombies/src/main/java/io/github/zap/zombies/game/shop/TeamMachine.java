package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hologram.HologramReplacement;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
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

/**
 * Machine with various tasks helpful for teams
 */
public class TeamMachine extends BlockShop<TeamMachineData> {

    private final Inventory inventory;

    private final Map<Integer, TeamMachineTask> slotMap = new HashMap<>();

    public TeamMachine(ZombiesArena zombiesArena, TeamMachineData shopData) {
        super(zombiesArena, shopData);

        this.inventory = prepareInventory();

        HologramReplacement hologram = getHologram();
        hologram.updateLine(0);
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

                        LocalizationManager localizationManager = getLocalizationManager();
                        for (Player player : zombiesArena.getWorld().getPlayers()) {
                            localizationManager.sendLocalizedMessage(
                                    player,
                                    MessageKey.TEAM_MACHINE_PURCHASE.getKey(),
                                    player.getDisplayName(),
                                    teamMachineTask.getDisplayName()
                            );
                        }

                        onPurchaseSuccess(zombiesPlayer);
                    }
                }
            }
        });
    }

    @Override
    protected void displayTo(Player player) {
        HologramReplacement hologram = getHologram();

        LocalizationManager localizationManager = getLocalizationManager();
        hologram.updateLineForPlayer(player, 0, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +
                localizationManager.getLocalizedMessageFor(player, MessageKey.TEAM_MACHINE.getKey()));

        hologram.updateLineForPlayer(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.REQUIRES_POWER.getKey())
                        : ChatColor.GREEN
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.RIGHT_CLICK_TO_OPEN.getKey())
        );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            Player player = args.getManagedPlayer().getPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                player.openInventory(inventory);
                return true;
            } else {
                getLocalizationManager().sendLocalizedMessage(player, MessageKey.NO_POWER.getKey());
            }
        }

        return false;
    }

    /**
     * Uses magic from TachibanaYui to choose the slots which correspond
     * to team machine tasks within the team machine GUI
     * @return The resulting inventory
     */
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
        // TODO: localization aaaaaaaaa
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
