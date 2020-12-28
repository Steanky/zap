package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
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

import java.util.*;

public class LuckyChest extends Shop<LuckyChestData> {

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private final Random random = new Random();

    private final Block left, right;

    private int rolls = 0;

    private int rollIndex;

    private int sittingTask;

    private Hologram hologram;

    private Item rollingItem = null;

    private Player rollingPlayer = null;

    private boolean doneRolling = true;

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
    }

    @Override
    public void displayTo(Player player) {

    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer.getId().equals(rollingPlayer.getUniqueId())) {
            if (doneRolling) {
                // TODO: buy
                return true;
            } else {
                // TODO: not done
            }
        } else if (rollingPlayer == null) {
            if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                // TODO: poor
            } else {
                // TODO: start the chest
                recursiveGunSwap(0);

                rollingPlayer = zombiesPlayer.getPlayer();
                rolls++;
                doneRolling = false;

                return true;
            }
        } else {
            // TODO: someone else rolling
        }
        return false;
    }

    private void recursiveGunSwap(int level) {
        Zombies zombies = Zombies.getInstance();
        LuckyChestData luckyChestData = getShopData();
        List<ImmutableTriple<Sound, Float, Long>> jingle = luckyChestData.getJingle();

        if (level == jingle.size()) {
            doneRolling = true;
            sittingTask = new BukkitRunnable() {

                private float sittingTime = luckyChestData.getSittingTime();

                @Override
                public void run() {
                    sittingTime -= 0.1;
                    if (sittingTime == 0) {
                        cancel();
                    }
                }
            }.runTaskTimer(zombies, 0L, 2L).getTaskId();
        } else {
            World world = getZombiesArena().getWorld();
            if (level == 0) {
                rollingItem = world.dropItem(
                        luckyChestData.getChestLocation().toLocation(world).add(0, 0.5, 0),
                        new ItemStack(Material.AIR)
                );
            }

            ImmutableTriple<Sound, Float, Long> jingleSound = jingle.get(level);
            new BukkitRunnable() {
                @Override
                public void run() {
                    World world = getZombiesArena().getWorld();
                    world.playSound(
                            luckyChestData.getChestLocation().toLocation(world),
                            jingleSound.left,
                            jingleSound.middle,
                            1.0F
                    );
                    rollIndex = random.nextInt(jingle.size());
                    rollingItem.getItemStack().setType(equipments.get(rollIndex).getMaterial());

                    recursiveGunSwap(level + 1);
                }
            }.runTaskLater(zombies, 20 * jingleSound.right);
        }

    }

    private void cleanupSitting() {
        hologram.destroy();
        rollingItem.remove();
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

}
