package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.util.Jingle;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Chest used to randomly generate a weapon from a predefined set of weapons to present to the player
 */
public class LuckyChest extends Shop<LuckyChestData> {

    @Getter
    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    @Getter
    private final Location chestLocation;

    private final Block left, right;

    private final Roller roller;

    private UUID rollingPlayerId = null;

    private Hologram hologram;

    @Setter
    private boolean active;

    public LuckyChest(ZombiesArena zombiesArena, LuckyChestData shopData) {
        super(zombiesArena, shopData);

        EquipmentManager equipmentManager = zombiesArena.getEquipmentManager();
        String mapNameKey = getZombiesArena().getMap().getMapNameKey();
        for (String equipmentName : shopData.getEquipments()) {
            equipments.add(equipmentManager.getEquipmentData(mapNameKey, equipmentName));
        }

        World world = zombiesArena.getWorld();
        chestLocation = getShopData().getChestLocation().toLocation(world);
        Block block = world.getBlockAt(chestLocation);
        Chest chest = (Chest) block.getState();
        DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
        DoubleChestInventory doubleChestInventory = (DoubleChestInventory) Objects.requireNonNull(doubleChest).getInventory();
        left = world.getBlockAt(Objects.requireNonNull(doubleChestInventory.getLeftSide().getLocation()));
        right = world.getBlockAt(Objects.requireNonNull(doubleChestInventory.getRightSide().getLocation()));

        roller = new Roller(this);
    }

    /**
     * Enables or disables the lucky chest
     * @param enable Whether or not to enable the lucky chest
     */
    public void toggle(boolean enable) {
        if (enable) {
            hologram = new Hologram(chestLocation.clone().add(0, 0.5, 0));
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine("");
            }

            active = true;
        } else if (hologram != null) {
            hologram.destroy();
            hologram = null;

            active = false;
        }
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        for (Player player : args.getPlayers()) {
            if (hologram != null) {
                hologram.renderToPlayer(player);
                roller.displayTo(player);
            }
        }
    }

    @Override
    public void display() {
        if (hologram != null) {
            LuckyChestData luckyChestData = getShopData();

            hologram.updateLineForEveryone(0, ChatColor.GOLD + "Lucky Chest");
            hologram.updateLineForEveryone(1,
                    luckyChestData.isRequiresPower() && !isPowered()
                            ? ChatColor.GRAY + "Requires Power!"
                            : String.format("%s%d Gold", ChatColor.GOLD, luckyChestData.getCost())
            );
        }
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();

        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) args.getEvent();
            if (left.equals(playerInteractEvent.getClickedBlock())
                    || right.equals(playerInteractEvent.getClickedBlock())) {
                ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
                Player player = zombiesPlayer.getPlayer();

                if (!getShopData().isRequiresPower() || isPowered()) {
                    if (active) {
                        LuckyChestData luckyChestData = getShopData();

                        if (rollingPlayerId == null) {
                            if (zombiesPlayer.getCoins() < luckyChestData.getCost()) {
                                zombiesPlayer.getPlayer()
                                        .sendMessage(ChatColor.RED + "You cannot afford this item!");
                            } else {
                                rollingPlayerId = zombiesPlayer.getPlayer().getUniqueId();
                                playerInteractEvent.setCancelled(true);
                                Jingle.play(luckyChestData.getJingle(), roller, chestLocation);
                            }
                        } else if (zombiesPlayer.getId().equals(rollingPlayerId)) {
                            if (roller.isCollectable()) {
                                EquipmentData<?> equipmentData = equipments.get(roller.getRollIndex());

                                HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                                EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup) hotbarManager
                                        .getHotbarObjectGroup(equipmentData.getName());

                                if (equipmentObjectGroup == null) {
                                    player.sendMessage(ChatColor.RED + "It looks like you cannot obtain this item!");
                                } else {
                                    Integer slot = equipmentObjectGroup.getNextEmptySlot();
                                    if (slot == null) {
                                        player.sendMessage(ChatColor.RED + "Choose a slot to receive the item in!");
                                    } else {
                                        hotbarManager.setHotbarObject(slot, getZombiesArena().getEquipmentManager().createEquipment(
                                                zombiesPlayer,
                                                slot,
                                                equipmentData
                                        ));
                                        roller.cancelSitting();
                                        rollingPlayerId = null;

                                        onPurchaseSuccess(zombiesPlayer);
                                    }
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "The chest is not done rolling yet!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Somebody else is rolling!");
                        }
                    } else {
                        // TODO: not active rn
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "The power is not active yet!");
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.LUCKY_CHEST;
    }

    /**
     * Utility class to roll the guns in the chest's display
     */
    private class Roller implements Jingle.JingleListener {

        private final Random random = new Random();

        private final Zombies zombies;

        private final ProtocolManager protocolManager;

        private final World world;

        private final Location chestLocation;

        private final long sittingTime;

        private final List<EquipmentData<?>> equipments;

        private Hologram timeRemaining;

        private Hologram rightClickToClaim;

        private Hologram gunName;

        private Item rollingItem;

        @Getter
        private int rollIndex;

        @Getter
        private boolean collectable = false;

        private int sittingTaskId;

        public Roller(LuckyChest luckyChest) {
            this.zombies = Zombies.getInstance();
            this.protocolManager = ProtocolLibrary.getProtocolManager();
            this.chestLocation = luckyChest.getChestLocation();
            this.world = chestLocation.getWorld();
            this.equipments = luckyChest.getEquipments();

            LuckyChestData luckyChestData = luckyChest.getShopData();
            this.sittingTime = luckyChestData.getSittingTime();
        }
        /**
         * Displays all relevant holograms to a player
         * @param player The player to display the holograms to
         */
        private void displayTo(Player player) {
            if (timeRemaining != null) {
                timeRemaining.renderToPlayer(player);
            }

            if (rightClickToClaim != null) {
                rightClickToClaim.renderToPlayer(player);
            }

            if (gunName != null) {
                gunName.renderToPlayer(player);
            }

            try {
                protocolManager.sendServerPacket(player, getChestPacket());
            } catch (InvocationTargetException exception) {
                zombies.getLogger().warning(
                        String.format("Error sending chest packet to player %s", player.getName())
                );
            }
        }

        /**
         * Creates a packet for displaying whether or not the chest is open
         */
        private PacketContainer getChestPacket() {
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
            packetContainer.getBlockPositionModifier().write(0, new BlockPosition(chestLocation.toVector()));
            packetContainer.getIntegers()
                    .write(0, 1)
                    .write(1, (rollingItem == null) ? 0 : 1);

            return packetContainer;
        }

        @Override
        public void onStart(List<Pair<List<Jingle.Note>, Long>> jingle) {
            toggle(false);
            rollingItem = world.dropItem(
                    chestLocation.clone().add(0, 0.981250, 0),
                    new ItemStack(Material.AIR)
            );
            rollingItem.setGravity(false);
            rollingItem.setVelocity(new Vector(0, 0, 0));

            gunName = new Hologram(chestLocation.clone(), 1);
            gunName.addLine("");

            PacketContainer packetContainer = getChestPacket();
            for (Player player : chestLocation.getWorld().getPlayers()) {
                try {
                    protocolManager.sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException exception) {
                    zombies.getLogger().warning(
                            String.format("Error sending chest packet to player %s", player.getName())
                    );
                }
            }
        }


        @Override
        public void onEnd(List<Pair<List<Jingle.Note>, Long>> jingle) {
            timeRemaining = new Hologram(
                    chestLocation.clone().add(0, 1, 0),
                    2
            );
            timeRemaining.addLine(String.format("%s%ds", ChatColor.RED, sittingTime));

            rightClickToClaim = new Hologram(
                    chestLocation.clone().add(0, 0.25, 0),
                    1
            );
            rightClickToClaim.addLine("Right Click to Claim");

            collectable = true;
            sittingTaskId = new BukkitRunnable() {

                private long sittingTime = Roller.this.sittingTime;

                @Override
                public void run() {
                    timeRemaining.updateLineForEveryone(
                            0,
                            String.format("%s%ds", ChatColor.RED, sittingTime -= 0.1)
                    );
                    if (sittingTime <= 0) {
                        cancelSitting();
                    }
                }
            }.runTaskTimer(zombies, 0, 2).getTaskId();
        }

        @Override
        public void onNotePlayed(List<Pair<List<Jingle.Note>, Long>> jingle) {
            EquipmentData<?> equipmentData = equipments.get(rollIndex = random.nextInt(jingle.size()));

            rollingItem.getItemStack().setType(equipmentData.getMaterial());
            gunName.updateLineForEveryone(0, equipmentData.getName());
        }

        /**
         * Stops the gun from sitting in the lucky chest's display
         */
        public void cancelSitting() {
            timeRemaining.destroy();
            timeRemaining = null;

            rightClickToClaim.destroy();
            rightClickToClaim = null;

            gunName.destroy();
            gunName = null;

            rollingItem.remove();
            rollingItem = null;

            collectable = false;

            PacketContainer packetContainer = getChestPacket();
            for (Player player : chestLocation.getWorld().getPlayers()) {
                try {
                    protocolManager.sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException exception) {
                    zombies.getLogger().warning(
                            String.format("Error sending chest packet to player %s", player.getName())
                    );
                }
            }

            Bukkit.getScheduler().cancelTask(sittingTaskId);
            toggle(true);
        }

    }

}
