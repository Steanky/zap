package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import lombok.Getter;
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

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private final Block left, right;

    private final Roller roller;

    private int rolls = 0;

    private Hologram hologram;

    public LuckyChest(ZombiesArena zombiesArena, LuckyChestData shopData) {
        super(zombiesArena, shopData);

        EquipmentManager equipmentManager = zombiesArena.getEquipmentManager();
        String mapNameKey = getZombiesArena().getMap().getMapNameKey();
        for (String equipmentName : shopData.getEquipments()) {
            equipments.add(equipmentManager.getEquipmentData(mapNameKey, equipmentName));
        }

        World world = zombiesArena.getWorld();
        Location chestLocation = getShopData().getChestLocation().toLocation(world);
        Block block = world.getBlockAt(chestLocation);
        Chest chest = (Chest) block.getState();
        DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
        DoubleChestInventory doubleChestInventory = (DoubleChestInventory) Objects.requireNonNull(doubleChest).getInventory();
        left = world.getBlockAt(Objects.requireNonNull(doubleChestInventory.getLeftSide().getLocation()));
        right = world.getBlockAt(Objects.requireNonNull(doubleChestInventory.getRightSide().getLocation()));

        roller = new Roller(this, chestLocation, equipments);
    }

    @Override
    public void displayTo(Player player) {

    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        LuckyChestData luckyChestData = getShopData();
        if (roller.getRoller() == null) {
            if (zombiesPlayer.getCoins() < luckyChestData.getCost()) {
                // TODO: poor
            } else {
                roller.start(zombiesPlayer.getPlayer());
            }
        } else if (zombiesPlayer.getId().equals(roller.getRoller().getUniqueId())) {
            if (roller.isCollectable()) {
                roller.cancelSitting();
                return true;
            } else {
                // TODO: still rolling
            }
        } else {
            // TODO: you're not the roller
        }

        return false;
    }

    @Override
    public boolean tryInteractWith(ZombiesArena.ProxyArgs<? extends Event> object) {
        Event event = object.getEvent();
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            Block block = playerInteractEvent.getClickedBlock();
            if (left.equals(block) || right.equals(block)) {
                playerInteractEvent.setCancelled(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.LUCKY_CHEST.name();
    }

    private static class Roller {

        private final LuckyChest luckyChest;

        private final Random random = new Random();

        private final Zombies zombies;

        private final World world;

        private final Location chestLocation;

        private final List<ImmutableTriple<Sound, Float, Long>> jingle;

        private final long sittingTime;

        private final List<EquipmentData<?>> equipments;

        @Getter
        private Player roller;

        private Hologram timeRemaining;

        private Hologram rightClickToClaim;

        private Hologram gunName;

        private Item rollingItem;

        private int rollIndex;

        @Getter
        private boolean collectable = false;

        private int sittingTaskId;

        public Roller(LuckyChest luckyChest, Location chestLocation,  List<EquipmentData<?>> equipments) {
            this.luckyChest = luckyChest;
            this.zombies = Zombies.getInstance();
            this.world = chestLocation.getWorld();
            this.chestLocation = chestLocation;
            this.equipments = equipments;

            LuckyChestData luckyChestData = luckyChest.getShopData();
            this.jingle = luckyChestData.getJingle();
            this.sittingTime = luckyChestData.getSittingTime();
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
                        rollingItem.getItemStack().setType(equipments.get(
                                rollIndex = random.nextInt(jingle.size())
                        ).getMaterial());
                        world.playSound(rollingItem.getLocation(), sound.getLeft(), sound.getMiddle(), 1.0F);

                        gunSwap(occurrence + 1);
                    }
                }.runTaskLater(zombies, 20 * sound.getRight());
            } else {
                timeRemaining = new Hologram(chestLocation.clone().add(0, 1, 0), 2);
                String format = ChatColor.RED + "%ds";
                timeRemaining.setLine(0, String.format(format, sittingTime));

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
            rightClickToClaim.destroy();
            gunName.destroy();
            rollingItem.remove();

            Bukkit.getScheduler().cancelTask(sittingTaskId);
            roller = null;
            collectable = false;
        }

    }

}
