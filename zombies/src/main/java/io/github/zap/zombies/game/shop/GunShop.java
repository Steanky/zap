package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.GunShopData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
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
        getZombiesArena().getShopEvents().get(getShopType()).registerHandler(args -> {
            if (args.getShop().equals(this)) {
                displayTo(args.getZombiesPlayer().getPlayer());
            }
        });
    }

    @Override
    public void display() {
        if (item == null) {
            World world = getZombiesArena().getWorld();

            ItemStack itemStack = new ItemStack(
                    getZombiesArena().getEquipmentManager().getEquipmentData(
                            getZombiesArena().getMap().getMapNameKey(), getShopData().getGunName()
                    ).getMaterial()
            );
            item = world.dropItem(
                    getShopData().getRootLocation().toLocation(world).add(0.5, 0.48125, 0.5),
                    itemStack
            );
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));
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
                                firstHologramLine = String.format("%sRefill %s", ChatColor.GREEN, gunName);
                                secondHologramLine =
                                        String.format("%s%d Gold", ChatColor.GREEN, gunShopData.getRefillCost());
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (firstHologramLine == null) {
            firstHologramLine = String.format("%sBuy %s", ChatColor.GREEN, gunName);
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
            Player player = zombiesPlayer.getPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                GunObjectGroup gunObjectGroup = (GunObjectGroup)
                        hotbarManager.getHotbarObjectGroup(EquipmentType.GUN.name());

                if (tryRefill(zombiesPlayer, gunObjectGroup) != null || tryBuy(zombiesPlayer, gunObjectGroup)) {
                    onPurchaseSuccess(zombiesPlayer);
                    // TODO: ye
                }
            } else {
                player.sendMessage(ChatColor.RED + "The power is not active yet!");
            }

            return true;
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
                    getZombiesArena().getMap().getMapNameKey(),
                    gunShopData.getGunName()));

            return true;
        }
    }

}
