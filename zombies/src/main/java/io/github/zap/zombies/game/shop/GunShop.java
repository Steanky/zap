package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.map.shop.GunShopData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Shop used to purchase guns
 */
public class GunShop extends ArmorStandShop<GunShopData> {

    private Item item = null;

    public GunShop(ZombiesArena zombiesArena, GunShopData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();
        getZombiesArena().getShopEvent(getShopType()).registerHandler(args -> {
            if (args.getShop().equals(this)) {
                Player player = args.getZombiesPlayer().getPlayer();
                if (player != null) {
                    displayTo(args.getZombiesPlayer().getPlayer());
                }
            }
        });
    }

    @Override
    public void display() {
        if (item == null) {
            World world = getZombiesArena().getWorld();

            ZombiesArena zombiesArena = getZombiesArena();
            EquipmentData<?> equipmentData = zombiesArena.getEquipmentManager().getEquipmentData(
                    zombiesArena.getMap().getName(),
                    getShopData().getGunName()
            );

            if(equipmentData == null) {
                Zombies.warning("Unable to find equipment data for weapon " + getShopData().getGunName() + "!");
                return;
            }

            ItemStack itemStack = new ItemStack(equipmentData.getMaterial());
            item = world.dropItem(
                    getShopData().getRootLocation().toLocation(world).add(new Vector(0.5D, 0, 0.5D)),
                    itemStack
            );
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));

            zombiesArena.getProtectedItems().add(item);
        }
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine("");
        }

        super.display();
    }

    @Override
    protected void displayTo(Player player) {
        ZombiesPlayer zombiesPlayer =  getZombiesArena().getPlayerMap().get(player.getUniqueId());
        GunShopData gunShopData = getShopData();
        String gunName = gunShopData.getGunName();
        String gunDisplayName = gunShopData.getGunDisplayName();

        String firstHologramLine = null;
        String secondHologramLine = null;

        if (gunShopData.isRequiresPower() && !isPowered()) {
            secondHologramLine = ChatColor.GRAY + "Requires Power!";
        } else {
            if (zombiesPlayer != null) {
                GunObjectGroup gunObjectGroup
                        = (GunObjectGroup) zombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentType.GUN.name());
                if (gunObjectGroup != null) {
                    for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?>) {
                            Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;

                            if (gun.getEquipmentData().getName().equals(gunName)) {
                                firstHologramLine = String.format("%sRefill %s", ChatColor.GREEN, gunDisplayName);
                                secondHologramLine =
                                        String.format("%s%d Gold", ChatColor.GOLD, gunShopData.getRefillCost());
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (firstHologramLine == null) {
            firstHologramLine = String.format("%sBuy %s", ChatColor.GREEN, gunDisplayName);
            secondHologramLine = String.format("%s%d Gold", ChatColor.GOLD, gunShopData.getCost());
        }

        Hologram hologram = getHologram();

        hologram.updateLineForPlayer(player, 0, firstHologramLine);
        hologram.updateLineForPlayer(player, 1, secondHologramLine);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            if (zombiesPlayer != null) {
                Player player = zombiesPlayer.getPlayer();

                if (player != null) {
                    if (!getShopData().isRequiresPower() || isPowered()) {
                        HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                        GunObjectGroup gunObjectGroup = (GunObjectGroup)
                                hotbarManager.getHotbarObjectGroup(EquipmentType.GUN.name());

                        Boolean refillAttempt = tryRefill(zombiesPlayer, gunObjectGroup);
                        if (refillAttempt == null) {
                            if (tryBuy(zombiesPlayer, gunObjectGroup)) {
                                player.playSound(Sound.sound(
                                        Key.key("block.note_block.pling"),
                                        Sound.Source.MASTER,
                                        1.0F,
                                        2.0F
                                ));
                                onPurchaseSuccess(zombiesPlayer);
                                return true;
                            }
                        } else if (refillAttempt) {
                            player.playSound(Sound.sound(
                                    Key.key("block.note_block.pling"),
                                    Sound.Source.MASTER,
                                    1.0F,
                                    2.0F
                            ));
                            onPurchaseSuccess(zombiesPlayer);
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The power is not active yet!");
                    }

                    player.playSound(Sound.sound(
                            Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER,
                            1.0F,
                            0.5F
                    ));

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.GUN_SHOP;
    }

    private Boolean tryRefill(ZombiesPlayer zombiesPlayer, GunObjectGroup gunObjectGroup) {
        GunShopData gunShopData = getShopData();

        for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<?, ?>) {
                Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;

                if (gun.getEquipmentData().getName().equals(gunShopData.getGunName())) {
                    if (gun.getCurrentAmmo() == gun.getCurrentLevel().getAmmo()) {
                        zombiesPlayer.getPlayer().sendMessage(ChatColor.RED + "Your gun is already filled!");
                        return false;
                    } else {
                        int refillCost = gunShopData.getRefillCost();
                        if (zombiesPlayer.getCoins() < refillCost) {
                            zombiesPlayer.getPlayer().sendMessage(ChatColor.RED + "You cannot afford this item!");

                            return false;
                        } else {
                            zombiesPlayer.subtractCoins(refillCost);
                            gun.refill();

                            return true;
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean tryBuy(ZombiesPlayer zombiesPlayer, GunObjectGroup gunObjectGroup) {
        Player player = zombiesPlayer.getPlayer();
        GunShopData gunShopData = getShopData();

        Integer slot = gunObjectGroup.getNextEmptySlot();
        if (slot == null) {
            int selectedSlot = player.getInventory().getHeldItemSlot();
            if (gunObjectGroup.getHotbarObjectMap().containsKey(selectedSlot)) {
                slot = selectedSlot;
            } else {
                player.sendMessage(ChatColor.RED + "Choose the slot you want to buy the gun in!");
                return false;
            }
        }

        int cost = gunShopData.getCost();
        if (zombiesPlayer.getCoins() < cost) {
            player.sendMessage(ChatColor.RED + "You cannot afford this item!");

            return false;
        } else {
            zombiesPlayer.subtractCoins(getShopData().getCost());
            gunObjectGroup.setHotbarObject(slot, getZombiesArena().getEquipmentManager().createEquipment(
                    getZombiesArena(),
                    zombiesPlayer,
                    slot,
                    getZombiesArena().getMap().getName(),
                    gunShopData.getGunName()));

            return true;
        }
    }

}
