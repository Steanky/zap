package io.github.zap.zombies.game.shop;

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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutableTriple;
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

import java.util.*;

public class LuckyChest extends Shop<LuckyChestData> {

    @Getter
    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    @Getter
    private final Location chestLocation;

    private final Block left, right;

    private final Roller roller;

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

    public void toggle(boolean show) {
        if (show) {
            hologram = new Hologram(chestLocation.clone().add(0, 0.5, 0));
        } else if (hologram != null) {
            hologram.destroy();
            hologram = null;
        }
    }

    @Override
    public void displayTo(Player player) {
        if (hologram != null) {
            hologram.renderTo(player);
        }
        roller.displayTo(player);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            if (left.equals(playerInteractEvent.getClickedBlock())
                    || right.equals(playerInteractEvent.getClickedBlock())) {
                ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
                Player player = zombiesPlayer.getPlayer();
                if (active) {
                    LuckyChestData luckyChestData = getShopData();
                    if (roller.getRoller() == null) {
                        if (zombiesPlayer.getCoins() < luckyChestData.getCost()) {
                            // TODO: poor
                        } else {
                            roller.start(player);
                        }
                    } else if (zombiesPlayer.getId().equals(roller.getRoller().getUniqueId())) {
                        if (roller.isCollectable()) {
                            EquipmentData<?> equipmentData = equipments.get(roller.getRollIndex());
                            HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                            EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup) hotbarManager
                                    .getHotbarObjectGroup(equipmentData.getName());
                            Integer slot = equipmentObjectGroup.getNextEmptySlot();
                            if (slot == null) {
                                // TODO: choose a slot
                            } else {
                                hotbarManager.setHotbarObject(slot, getZombiesArena().getEquipmentManager().createEquipment(
                                        player,
                                        slot,
                                        equipmentData
                                ));
                                roller.cancelSitting();

                                onPurchaseSuccess(zombiesPlayer);
                            }
                        } else {
                            // TODO: still rolling
                        }
                    } else {
                        // TODO: you're not the roller
                    }
                } else {
                    // TODO: not active rn
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.LUCKY_CHEST.name();
    }

    private static class Roller {

        private final Random random = new Random();

        private final Zombies zombies;

        private final World world;

        private final Location chestLocation;

        private final List<ImmutableTriple<Sound, Float, Long>> jingle;

        private final long sittingTime;

        private final List<EquipmentData<?>> equipments;


        private final static String format = ChatColor.RED + "%ds";

        @Getter
        private Player roller;

        private Hologram timeRemaining;

        private String timeRemainingString;

        private Hologram rightClickToClaim;

        private String rightClickToClaimString = "";

        private Hologram gunName;

        private String gunNameString;

        private Item rollingItem;

        @Getter
        private int rollIndex;

        @Getter
        private boolean collectable = false;

        private int sittingTaskId;

        private final LuckyChest luckyChest;

        public Roller(LuckyChest luckyChest) {
            this.luckyChest = luckyChest;
            this.zombies = Zombies.getInstance();
            this.chestLocation = luckyChest.getChestLocation();
            this.world = chestLocation.getWorld();
            this.equipments = luckyChest.getEquipments();

            LuckyChestData luckyChestData = luckyChest.getShopData();
            this.jingle = luckyChestData.getJingle();
            this.sittingTime = luckyChestData.getSittingTime();
        }

        private void displayTo(Player player) {
            if (timeRemaining != null) {
                timeRemaining.renderTo(player);
                timeRemaining.setLineFor(player, 0, timeRemainingString);
            }
            if (rightClickToClaim != null) {
                rightClickToClaim.renderTo(player);
                rightClickToClaim.setLineFor(player, 0, rightClickToClaimString);
            }
            if (gunName != null) {
                gunName.renderTo(player);
                gunName.setLineFor(player, 0, gunNameString);
            }
        }

        public void start(Player roller) {
            this.roller = roller;
            gunSwap(0);
        }

        private void gunSwap(int occurrence) {
            if (occurrence < jingle.size()) {
                if (occurrence == 0) {
                    rollingItem = world.dropItem(
                            chestLocation.clone().add(0, 0.981250, 0),
                            new ItemStack(Material.AIR)
                    );
                    rollingItem.setGravity(false);
                    rollingItem.setVelocity(new Vector(0, 0, 0));

                    gunName = new Hologram(chestLocation.clone(), 1);
                }

                ImmutableTriple<Sound, Float, Long> sound = jingle.get(occurrence);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        EquipmentData<?> equipmentData = equipments.get(rollIndex = random.nextInt(jingle.size()));
                        rollingItem.getItemStack().setType(equipmentData.getMaterial());
                        gunName.setLine(0, gunNameString = equipmentData.getName());
                        world.playSound(rollingItem.getLocation(), sound.getLeft(), sound.getMiddle(), 1.0F);

                        gunSwap(occurrence + 1);
                    }
                }.runTaskLater(zombies, 20 * sound.getRight());
            } else {
                timeRemaining = new Hologram(chestLocation.clone().add(0, 1, 0), 2);
                timeRemaining.setLine(0, timeRemainingString = String.format(format, sittingTime));

                rightClickToClaim = new Hologram(chestLocation.clone().add(0, 0.25, 0), 1);

                collectable = true;
                sittingTaskId = new BukkitRunnable() {

                    private long sittingTime = Roller.this.sittingTime;

                    @Override
                    public void run() {
                        timeRemaining.setLine(0, String.format(format, sittingTime -= 0.1));
                        if (sittingTime <= 0) {
                            cancelSitting();
                        }
                    }
                }.runTaskTimer(zombies, 0, 2).getTaskId();
            }
        }

        public void cancelSitting() {
            timeRemaining.destroy();
            timeRemaining = null;
            rightClickToClaim.destroy();
            rightClickToClaim = null;
            gunName.destroy();
            gunName = null;
            rollingItem.remove();
            rollingItem = null;

            Bukkit.getScheduler().cancelTask(sittingTaskId);
            roller = null;
            collectable = false;
        }

    }

}
