package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Unique;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.TeamMachineData;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Machine with various tasks helpful for teams
 */
public class TeamMachine extends BlockShop<TeamMachineData> implements Unique {

    @Getter
    private final UUID id = UUID.randomUUID();

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
        zombiesArena.getProxyFor(InventoryClickEvent.class).registerHandler(args -> {
            InventoryClickEvent inventoryClickEvent = args.getEvent();

            if (inventory.equals(inventoryClickEvent.getClickedInventory())) {
                HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
                ZombiesPlayer zombiesPlayer = zombiesArena.getPlayerMap()
                        .get(humanEntity.getUniqueId());

                if (zombiesPlayer != null) {
                    inventoryClickEvent.setCancelled(true);
                    TeamMachineTask teamMachineTask = slotMap.get(inventoryClickEvent.getSlot());

                    if (teamMachineTask != null
                            && teamMachineTask.execute(this, zombiesArena, zombiesPlayer)) {

                        for (Player player : zombiesArena.getWorld().getPlayers()) {
                            player.sendMessage(
                                    String.format(
                                            "%sPlayer %s purchased %s from the Team Machine!",
                                            ChatColor.YELLOW,
                                            zombiesPlayer.getPlayer().getName(),
                                            teamMachineTask.getDisplayName()
                                    )
                            );
                        }
                        humanEntity.closeInventory();

                        Sound sound = Sound.sound(
                                Key.key("minecraft:entity.player.levelup"),
                                Sound.Source.MASTER,
                                1.0F,
                                1.5F
                                );
                        humanEntity.playSound(sound);

                        inventory.setItem(
                                inventoryClickEvent.getSlot(),
                                teamMachineTask.getItemStackRepresentationForTeamMachine(this)
                        );

                        onPurchaseSuccess(zombiesPlayer);
                    }
                }
            }
        });
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine("");
        }

        hologram.updateLineForEveryone(0, ChatColor.BLUE + "Team Machine");
        hologram.updateLineForEveryone(
                1,
                (getShopData().isRequiresPower() && !isPowered())
                        ? ChatColor.GRAY + "Requires Power!"
                        : ChatColor.GREEN + "Right click to open!"
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
                player.sendMessage("The power is not active yet!");
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
        Inventory inventory;
        List<TeamMachineTask> teamMachineTasks = getShopData().getTeamMachineTasks();
        int num = teamMachineTasks.size();

        if (num > 0) {
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
            inventory = Bukkit.createInventory(null, guiSize, Component.text("Team Machine"));

            int index = 0;

            for (int h = 0; h < height; h++) {
                int lineCount = (h == remainderLine) ? finalLine : width;
                for (int w = 0; w < lineCount && index < num; w++) {
                    int slot = (18 * w + 9) / (2 * lineCount);
                    int pos = (h + offset) * 9 + slot;

                    TeamMachineTask teamMachineTask = teamMachineTasks.get(index);
                    ItemStack teamMachineItemStackRepresentation
                            = teamMachineTask.getItemStackRepresentationForTeamMachine(this);

                    inventory.setItem(pos, teamMachineItemStackRepresentation);
                    slotMap.put(pos, teamMachineTask);

                    index++;
                }
            }
        } else {
            inventory = Bukkit.createInventory(null, 9, Component.text("Team Machine"));
        }

        return inventory;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.TEAM_MACHINE;
    }
}
