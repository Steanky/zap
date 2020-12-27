package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.GunShopData;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Objects;

public class GunShop extends ArmorStandShop<GunShopData> {

    public GunShop(ZombiesArena zombiesArena, GunShopData shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();
        BlockFace blockFace = getShopData().getBlockFace();
        Location location = getShopData().getBlockLocation().add(blockFace.getDirection()).toLocation(world);

        ItemStack itemStack = new ItemStack(
                zombiesArena.getEquipmentManager().getEquipmentData(getShopData().getGunName()).getMaterial()
        );
        Item item = world.dropItem(location.clone().add(0.5, 0.48125, 0.5), itemStack);
        item.setGravity(false);
        item.setVelocity(new Vector(0, 0, 0));
    }

    @Override
    public void displayTo(Player player) {
        ZombiesPlayer zombiesPlayer =  getZombiesArena().getPlayerMap().get(player.getUniqueId());
        Hologram hologram = getHologram();
        GunShopData gunShopData = getShopData();
        String gunName = gunShopData.getGunName();

        if (zombiesPlayer != null) {
            GunObjectGroup gunObjectGroup = (GunObjectGroup) zombiesPlayer.getHotbarManager()
                    .getHotbarObjectGroup(EquipmentType.GUN.toString());
            for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                if (hotbarObject instanceof Gun<?, ?>) {
                    Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;
                    if (gun.getEquipmentData().getName().equals(gunShopData.getGunName())) {
                        // TODO: localization
                        hologram.setLineFor(player, 0,
                                ChatColor.GREEN + gunName + " Ammo");
                        hologram.setLineFor(player, 1,
                                ChatColor.GOLD + String.valueOf(gunShopData.getRefillCost()));

                        return;
                    }
                }
            }
        }

        getHologram().setLineFor(player, 0, ChatColor.GREEN + gunName);
        getHologram().setLineFor(player, 1, ChatColor.GOLD + String.valueOf(gunShopData.getRefillCost()));
    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
        GunObjectGroup gunObjectGroup = (GunObjectGroup) hotbarManager.getHotbarObjectGroup(EquipmentType.GUN.toString());

        return Objects.requireNonNullElseGet(tryRefill(zombiesPlayer, gunObjectGroup), () -> tryBuy(zombiesPlayer, gunObjectGroup));
    }

    private Boolean tryRefill(ZombiesPlayer zombiesPlayer, GunObjectGroup gunObjectGroup) {
        GunShopData gunShopData = getShopData();
        for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<?, ?>) {
                Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;

                if (gun.getEquipmentData().getName().equals(gunShopData.getGunName())) {
                    int refillCost = gunShopData.getRefillCost();
                    if (zombiesPlayer.getCoins() < refillCost) {
                        // TODO: cannot refill
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
            if (gunObjectGroup.getSlots().contains(selectedSlot)) {
                slot = selectedSlot;
            } else {
                // TODO: choose a slot to put it in
                return false;
            }
        }
        int cost = gunShopData.getCost();
        if (zombiesPlayer.getCoins() < cost) {
            // TODO: cannot buy
            return false;
        } else {
            zombiesPlayer.subtractCoins(getShopData().getCost());
            gunObjectGroup.setHotbarObject(slot, getZombiesArena().getEquipmentManager().createEquipment(
                    zombiesPlayer.getPlayer(),
                    slot,
                    gunShopData.getGunName()
            ));
            displayTo(player);
            return true;
        }
    }

}
