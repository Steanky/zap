package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.Unique;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.TeamMachineData;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Machine with various tasks helpful for teams
 */
public class TeamMachine extends BlockShop<@NotNull TeamMachineData> implements Unique, Disposable {

    @Getter
    private final @NotNull UUID id = UUID.randomUUID();

    private final @NotNull Inventory inventory;

    private final @NotNull Map<Integer, TeamMachineTask> slotMap = new HashMap<>();

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    public TeamMachine(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull TeamMachineData shopData,
                       @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList) {
        super(world, eventManager, shopData);

        this.inventory = prepareInventory();
        this.playerList = playerList;
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();

        ZombiesArena zombiesArena = getArena();
        zombiesArena.getProxyFor(InventoryClickEvent.class).registerHandler(this::onInventoryClick);
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
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.player();
            if (!getShopData().isRequiresPower() || isPowered()) {
                player.getPlayer().openInventory(inventory);
                return true;
            } else {
                player.getPlayer().sendMessage(Component.text("The power is not active yet!",
                        NamedTextColor.RED));
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.TEAM_MACHINE.name();
    }

    @Override
    public void dispose() {
        Property.removeMappingsFor(this);
    }

    /**
     * Handler for inventory clicks to handle team machine events
     * @param args The arguments passed to the handler
     */
    private void onInventoryClick(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull InventoryClickEvent> args) {
        InventoryClickEvent inventoryClickEvent = args.event();

        if (inventory.equals(inventoryClickEvent.getClickedInventory())) {
            HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
            ZombiesArena arena = getArena();
            ZombiesPlayer player = playerList.getOnlinePlayer(humanEntity.getUniqueId());

            if (player != null) {
                inventoryClickEvent.setCancelled(true);
                TeamMachineTask teamMachineTask = slotMap.get(inventoryClickEvent.getSlot());

                if (teamMachineTask != null
                        && teamMachineTask.execute(this, arena, player)) {
                    Sound sound = Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER,
                            1.0F, 1.5F);
                    for (Player otherBukkitPlayer : arena.getWorld().getPlayers()) {
                        otherBukkitPlayer.sendMessage(
                                String.format("%sPlayer %s purchased %s from the Team Machine!", ChatColor.YELLOW,
                                        player.getPlayer().getName(),
                                        teamMachineTask.getDisplayName()));
                        otherBukkitPlayer.playSound(sound);
                    }
                    humanEntity.closeInventory();

                    inventory.setItem(inventoryClickEvent.getSlot(),
                            teamMachineTask.getItemStackRepresentationForTeamMachine(this)); // update costs

                    onPurchaseSuccess(player);
                }
            }
        }
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

}
