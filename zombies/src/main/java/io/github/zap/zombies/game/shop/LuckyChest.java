package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
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

            LocalizationManager localizationManager = getLocalizationManager();
            hologram.setLineFor(player, 0, ChatColor.DARK_PURPLE +
                    localizationManager.getLocalizedMessageFor(player, MessageKey.LUCKY_CHEST.getKey()));
            hologram.setLineFor(player, 1,
                    luckyChestData.isRequiresPower() && !isPowered()
                            ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
                            + localizationManager.getLocalizedMessageFor(player, MessageKey.REQUIRES_POWER.getKey())
                            : ChatColor.YELLOW.toString() + ChatColor.BOLD.toString()
                            + luckyChestData.getCost() + " "
                            + localizationManager.getLocalizedMessageFor(player, MessageKey.GOLD.getKey())
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

                        if (rollingPlayerId == null) {
                            if (zombiesPlayer.getCoins() < luckyChestData.getCost()) {
                                localizationManager.sendLocalizedMessage(player, MessageKey.CANNOT_AFFORD.getKey());
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
                                    localizationManager.sendLocalizedMessage(player, MessageKey.NO_GROUP.getKey());
                                } else {
                                    Integer slot = equipmentObjectGroup.getNextEmptySlot();
                                    if (slot == null) {
                                        localizationManager.sendLocalizedMessage(player,
                                                MessageKey.CHOOSE_SLOT.getKey());
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
                                localizationManager.sendLocalizedMessage(player, MessageKey.NOT_DONE_ROLLING.getKey());
                            }
                        } else {
                            localizationManager.sendLocalizedMessage(player, MessageKey.OTHER_PERSON_ROLLING.getKey());
                        }
                    } else {
                        // TODO: not active rn
                    }
                } else {
                    localizationManager.sendLocalizedMessage(player, MessageKey.NO_POWER.getKey());
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
    private static class Roller implements Jingle.JingleListener {

        private final Random random = new Random();

        private final Zombies zombies;

        private final ProtocolManager protocolManager;

        private final World world;

        private final Location chestLocation;

        private final long sittingTime;

        private final List<EquipmentData<?>> equipments;


        private final static String format = ChatColor.RED + "%ds";

        private Hologram timeRemaining;

        private String timeRemainingString;

        private Hologram rightClickToClaim;

        private Hologram gunName;

        private String gunNameString;

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
                timeRemaining.renderTo(player);
                timeRemaining.setLineFor(player, 0, timeRemainingString);
            }

            if (rightClickToClaim != null) {
                rightClickToClaim.renderTo(player);

                LocalizationManager localizationManager = zombies.getLocalizationManager();
                rightClickToClaim.setLineFor(
                        player,
                        0,
                        localizationManager.getLocalizedMessageFor(player, MessageKey.RIGHT_CLICK_TO_CLAIM.getKey()
                        )
                );
            }

            if (gunName != null) {
                gunName.renderTo(player);
                gunName.setLineFor(player, 0, gunNameString);
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
        public void onStart(List<ImmutablePair<List<Jingle.Note>, Long>> jingle) {
            rollingItem = world.dropItem(
                    chestLocation.clone().add(0, 0.981250, 0),
                    new ItemStack(Material.AIR)
            );
            rollingItem.setGravity(false);
            rollingItem.setVelocity(new Vector(0, 0, 0));

            gunName = new Hologram(chestLocation.clone(), 1);

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
        public void onEnd(List<ImmutablePair<List<Jingle.Note>, Long>> jingle) {
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
        public void onNotePlayed(List<ImmutablePair<List<Jingle.Note>, Long>> jingle) {
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
        }

    }

}
