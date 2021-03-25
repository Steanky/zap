package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.util.Jingle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Chest used to randomly generate a weapon from a predefined set of weapons to present to the player
 */
public class LuckyChest extends Shop<LuckyChestData> {

    private final Location chestLocation;

    private final Block left, right;

    private final Hologram hologram;

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private boolean active = false;

    private GunSwapper gunSwapper;

    private boolean doneRolling = false;

    private UUID roller;

    public LuckyChest(ZombiesArena zombiesArena, LuckyChestData shopData) {
        super(zombiesArena, shopData);

        Vector chestLocation = shopData.getChestLocation();
        World world = zombiesArena.getWorld();
        Block block = world.getBlockAt(chestLocation.getBlockX(), chestLocation.getBlockY(), chestLocation.getBlockZ());

        Chest chest = (Chest) block.getState();
        DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();

        DoubleChestInventory doubleChestInventory = (DoubleChestInventory) doubleChest.getInventory();
        Location leftLocation = doubleChestInventory.getLeftSide().getLocation();
        Location rightLocation = doubleChestInventory.getRightSide().getLocation();

        left = world.getBlockAt(leftLocation);
        right = world.getBlockAt(rightLocation);

        this.chestLocation = leftLocation
                .add(0.5, 0, 0.5)
                .add(rightLocation.add(0.5, 0, 0.5))
                .multiply(0.5);

        hologram = new Hologram(this.chestLocation.clone());

        EquipmentManager equipmentManager = zombiesArena.getEquipmentManager();
        String mapName = zombiesArena.getMap().getName();
        for (String equipmentName : shopData.getEquipments()) {
            equipments.add(equipmentManager.getEquipmentData(mapName, equipmentName));
        }
    }

    public void setActive(boolean active) {
        if (active) {
            if (!this.active) {
                this.active = true;
                display();
            }
        } else if (this.active) {
            hologram.destroy();
            this.active = false;
            display();
        }
    }

    @Override
    public void display() {
        if (active && roller == null) {
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine("");
            }
            hologram.updateLineForEveryone(0, String.format("%s%sLucky Chest", ChatColor.GOLD, ChatColor.BOLD));
            hologram.updateLineForEveryone(
                    1,
                    getShopData().isRequiresPower() && !isPowered()
                    ? String.format("%sRequires Power!", ChatColor.GRAY)
                    : String.format("%s%s%d Gold", ChatColor.YELLOW, ChatColor.BOLD, getShopData().getCost())
            );

        }

        super.display();
    }

    @Override
    protected void displayTo(Player player) {
        super.displayTo(player);
        hologram.renderToPlayer(player);
        if (gunSwapper != null) {
            gunSwapper.renderToPlayer(player);
        }
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            Block clickedBlock = playerInteractEvent.getClickedBlock();

            if (left.equals(clickedBlock) || right.equals(clickedBlock)) {
                LuckyChestData luckyChestData = getShopData();
                ZombiesPlayer zombiesPlayer = args.getManagedPlayer();

                if (zombiesPlayer != null) {
                    Player player = zombiesPlayer.getPlayer();

                    if (player != null) {
                        if (luckyChestData.isRequiresPower() && !isPowered()) {
                            player.sendMessage(Component
                                    .text("The power is not turned on!")
                                    .color(NamedTextColor.RED)
                            );
                        } else if (!active) {
                            String notActive = "This Lucky Chest is not active right now!";
                            String luckyChestRoom = getZombiesArena().getMap().getNamedRoom(getZombiesArena().getLuckyChestRoom()).getRoomDisplayName();
                            if (luckyChestRoom != null) {
                                notActive += " Find the Lucky Chest in " + luckyChestRoom + "!";
                            }

                            player.sendMessage(Component
                                    .text(notActive)
                                    .color(NamedTextColor.RED));
                        } else if (roller != null) {
                            if (player.getUniqueId().equals(roller)) {
                                if (doneRolling) {
                                    EquipmentData<?> equipmentData = gunSwapper.currentEquipment;
                                    EquipmentObjectGroup equipmentObjectGroup
                                            = (EquipmentObjectGroup) zombiesPlayer.getHotbarManager()
                                            .getHotbarObjectGroup(equipmentData.getEquipmentType());

                                    if (equipmentObjectGroup != null) {
                                        boolean anyGuns = false;
                                        for (HotbarObject hotbarObject : equipmentObjectGroup.getHotbarObjectMap().values()) {
                                            if (hotbarObject instanceof Gun<?, ?>) {
                                                Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;
                                                if (gun.getEquipmentData().getName().equals(equipmentData.getName())) {
                                                    ((Gun<?, ?>) hotbarObject).refill();
                                                    anyGuns = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!anyGuns) {
                                            Integer nextSlot = equipmentObjectGroup.getNextEmptySlot();
                                            if (nextSlot == null) {
                                                int heldSlot = player.getInventory().getHeldItemSlot();
                                                if (equipmentObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                                                    nextSlot = heldSlot;
                                                }
                                            }
                                            if (nextSlot != null) {
                                                ZombiesArena zombiesArena = getZombiesArena();
                                                equipmentObjectGroup.setHotbarObject(
                                                        nextSlot,
                                                        zombiesArena.getEquipmentManager().createEquipment(
                                                                zombiesArena,
                                                                zombiesPlayer,
                                                                nextSlot,
                                                                equipmentData
                                                        )
                                                );

                                                gunSwapper.destroy();

                                                player.playSound(Sound.sound(
                                                        Key.key("minecraft:block.note_block.pling"),
                                                        Sound.Source.MASTER,
                                                        1.0F,
                                                        2.0F
                                                ));

                                                return true;
                                            } else {
                                                player.sendMessage(Component
                                                        .text("Choose a slot to receive the item in!")
                                                        .color(NamedTextColor.RED)
                                                );
                                            }
                                        }
                                    } else {
                                        player.sendMessage(Component
                                                .text("You can't claim this weapon!")
                                                .color(NamedTextColor.RED)
                                        );
                                    }
                                    gunSwapper.destroy();
                                } else {
                                    player.sendMessage(Component
                                            .text("The chest is not done rolling!")
                                            .color(NamedTextColor.RED));

                                }
                            } else {
                                player.sendMessage(Component
                                        .text("Someone else is rolling!")
                                        .color(NamedTextColor.RED));
                            }
                        } else {
                            int cost = getShopData().getCost();
                            if (args.getManagedPlayer().getCoins() < cost) {
                                player.sendMessage(Component
                                        .text("You don't have enough coins to do that!")
                                        .color(NamedTextColor.RED));
                            } else {
                                zombiesPlayer.subtractCoins(cost);
                                hologram.destroy();
                                roller = player.getUniqueId();
                                doneRolling = false;

                                Jingle.play(
                                        getZombiesArena(),
                                        getShopData().getJingle(),
                                        gunSwapper = new GunSwapper(zombiesPlayer),
                                        chestLocation.clone().add(0, 1, 0)
                                );
                                return true;
                            }
                        }

                        player.playSound(Sound.sound(
                                Key.key("minecraft:entity.enderman.teleport"),
                                Sound.Source.MASTER,
                                1.0F,
                                0.5F
                        ));
                    }
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

    private class GunSwapper implements Jingle.JingleListener {

        private final Random random = new Random();

        private final PacketContainer openChestContainer = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);

        private final ZombiesPlayer zombiesPlayer;

        private final Item item;

        private EquipmentData<?> currentEquipment;

        private int sittingTaskId;


        private final Hologram equipmentName;

        private final Hologram endHologram;

        public GunSwapper(ZombiesPlayer zombiesPlayer) {
            this.zombiesPlayer = zombiesPlayer;

            World world = zombiesPlayer.getPlayer().getWorld();
            openChestContainer.getBlockPositionModifier().write(
                    0,
                    new BlockPosition(chestLocation.toVector())
            );
            openChestContainer.getIntegers()
                    .write(0, 1)
                    .write(1, 1);
            for (Player player : world.getPlayers()) {
                showChestToPlayer(player);
            }

            item = world.dropItem(
                    chestLocation.clone().add(0, 1, 0),
                    new ItemStack((currentEquipment = equipments.get(random.nextInt(equipments.size()))).getMaterial())
            );
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));

            equipmentName = new Hologram(chestLocation.clone().subtract(0, 0.75, 0));
            equipmentName.addLine(ChatColor.RED + currentEquipment.getDisplayName());

            endHologram = new Hologram(chestLocation);
        }

        private void renderToPlayer(Player player) {
            equipmentName.renderToPlayer(player);
            showChestToPlayer(player);
        }

        private void showChestToPlayer(Player player) {
            ArenaApi.getInstance().sendPacketToPlayer(Zombies.getInstance(), player, openChestContainer);
        }

        @Override
        public void onNotePlayed(List<Jingle.Note> jingle) {
            currentEquipment = equipments.get(random.nextInt(equipments.size()));
            equipmentName.updateLineForEveryone(0, ChatColor.RED + currentEquipment.getDisplayName());

            item.setItemStack(new ItemStack(currentEquipment.getMaterial()));
        }

        @Override
        public void onEnd(List<Jingle.Note> jingle) {
            doneRolling = true;

            endHologram.addLine(ChatColor.RED + "Right Click to Claim!");
            endHologram.addLine("");

            sittingTaskId = getZombiesArena().runTaskTimer(0L, 2L, new Runnable() {

                private long sittingTime = getShopData().getSittingTime();

                @Override
                public void run() {
                    if (sittingTime > 0) {
                        String timeRemaining = TimeUtil.convertTicksToSecondsString(sittingTime);
                        endHologram.updateLineForEveryone(
                                1,
                                String.format("%s%s", ChatColor.RED, timeRemaining)
                        );

                        sittingTime -= 2;
                    } else {
                        destroy();
                    }
                }

            }).getTaskId();
        }


        public void destroy() {
            openChestContainer.getIntegers()
                    .write(1, 0);
            for (Player player : chestLocation.getWorld().getPlayers()) {
                showChestToPlayer(player);
            }

            equipmentName.destroy();
            endHologram.destroy();

            item.remove();
            Bukkit.getScheduler().cancelTask(sittingTaskId);

            roller = null;
            gunSwapper = null;
            display();

            onPurchaseSuccess(zombiesPlayer);
        }

    }

}
