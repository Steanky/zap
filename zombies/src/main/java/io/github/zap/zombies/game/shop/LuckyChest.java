package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
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

// TODO: protocollib
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

            active = true;
        } else if (hologram != null) {
            hologram.destroy();
            hologram = null;

            active = false;
        }
    }

    @Override
    protected void displayTo(Player player) {
        if (hologram != null) {
            LuckyChestData luckyChestData = getShopData();

            hologram.renderTo(player);

            hologram.setLineFor(player, 0, ChatColor.DARK_PURPLE + "Lucky Chest");
            hologram.setLineFor(player, 1,
                    luckyChestData.isRequiresPower() && !isPowered()
                            ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!"
                            : ChatColor.YELLOW.toString() + ChatColor.BOLD.toString()
                            + luckyChestData.getCost() + " Gold"
            );
        }
        roller.displayTo(player);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();

        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) args.getEvent();
            if (left.equals(playerInteractEvent.getClickedBlock()) || right.equals(playerInteractEvent.getClickedBlock())) {
                LocalizationManager localizationManager = getLocalizationManager();
                ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
                Player player = zombiesPlayer.getPlayer();

                if (!getShopData().isRequiresPower() || isPowered()) {
                    if (active) {
                        LuckyChestData luckyChestData = getShopData();

                        if (roller.getRoller() == null) {
                            if (zombiesPlayer.getCoins() < luckyChestData.getCost()) {
                                localizationManager.sendLocalizedMessage(player,
                                        ChatColor.RED + MessageKey.CANNOT_AFFORD.getKey());
                            } else {
                                roller.start(player);
                            }
                        } else if (zombiesPlayer.getId().equals(roller.getRoller().getUniqueId())) {
                            if (roller.isCollectable()) {
                                EquipmentData<?> equipmentData = equipments.get(roller.getRollIndex());

                                HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                                EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup) hotbarManager
                                        .getHotbarObjectGroup(equipmentData.getName());

                                if (equipmentObjectGroup == null) {
                                    // TODO: uncollectable
                                } else {
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
                } else {
                    localizationManager.sendLocalizedMessage(player,
                            ChatColor.RED + MessageKey.NO_POWER.getKey());
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.LUCKY_CHEST.name();
    }

    /**
     * Utility class to roll the guns in the chest's display
     */
    private static class Roller extends Jingle {

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
            super();
            this.luckyChest = luckyChest;
            this.zombies = Zombies.getInstance();
            this.chestLocation = luckyChest.getChestLocation();
            this.world = chestLocation.getWorld();
            this.equipments = luckyChest.getEquipments();

            LuckyChestData luckyChestData = luckyChest.getShopData();
            this.jingle = luckyChestData.getJingle();
            this.sittingTime = luckyChestData.getSittingTime();
        }

        /**
         * Displays all relevant holograms to a player
         * @param player The player to display the holograms to
         */
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

        /**
         * Starts the rolling
         * @param roller The player that initiated the rolling
         */
        public void start(Player roller) {
            this.roller = roller;
            play(chestLocation);
        }

        @Override
        public void playAt(Location location, int noteNumber) {
            if (noteNumber == 0) {
                rollingItem = world.dropItem(
                        chestLocation.clone().add(0, 0.981250, 0),
                        new ItemStack(Material.AIR)
                );
                rollingItem.setGravity(false);
                rollingItem.setVelocity(new Vector(0, 0, 0));

                gunName = new Hologram(chestLocation.clone(), 1);
            }
            super.playAt(location, noteNumber);
        }

        @Override
        protected void onEnd() {
            super.onEnd();
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

        @Override
        protected void onNotePlayed() {
            super.onNotePlayed();
            EquipmentData<?> equipmentData = equipments.get(rollIndex = random.nextInt(jingle.size()));

            rollingItem.getItemStack().setType(equipmentData.getMaterial());
            gunName.setLine(0, gunNameString = equipmentData.getName());
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

            roller = null;
            collectable = false;

            Bukkit.getScheduler().cancelTask(sittingTaskId);
        }

    }

}
