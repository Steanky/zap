package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.GunShopData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Shop used to purchase guns
 */
public class GunShop extends ArmorStandShop<@NotNull GunShopData> {

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull MapData map;

    private final @NotNull EquipmentManager equipmentManager;

    private final @NotNull Set<@NotNull Item> protectedItems;

    private Item item = null;

    public GunShop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull GunShopData shopData,
                   @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList, @NotNull MapData map,
                   @NotNull EquipmentManager equipmentManager, @NotNull Set<@NotNull Item> protectedItems) {
        super(world, eventManager, shopData);

        this.playerList = playerList;
        this.map = map;
        this.equipmentManager = equipmentManager;
        this.protectedItems = protectedItems;
    }

    @Override
    protected void registerShopEvents(@NotNull ShopEventManager eventManager) {
        super.registerShopEvents(eventManager);
        eventManager.getEvent(getShopType()).registerHandler(args ->
                displayToPlayer(args.player().getPlayer()));
        eventManager.getEvent(ShopType.LUCKY_CHEST.name()).registerHandler(args ->
                displayToPlayer(args.player().getPlayer()));
        eventManager.getEvent(ShopType.PIGLIN_SHOP.name()).registerHandler(args ->
                displayToPlayer(args.player().getPlayer()));
    }

    @Override
    public void display() {
        if (item == null) {
            World world = getWorld();

            EquipmentData<?> equipmentData = equipmentManager
                    .getEquipmentData(map.getName(), getShopData().getGunName());

            if (equipmentData == null) {
                Zombies.warning("Unable to find equipment data for weapon " + getShopData().getGunName() + "!");
                return;
            }

            ItemStack itemStack = new ItemStack(equipmentData.getMaterial());
            item = world.dropItem(getShopData().getRootLocation().toLocation(world)
                            .add(new Vector(0.5D, 0, 0.5D)), itemStack);
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));

            protectedItems.add(item);
        }

        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine("");
        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        ZombiesPlayer zombiesPlayer = playerList.getOnlinePlayer(player);
        GunShopData gunShopData = getShopData();
        String gunName = gunShopData.getGunName();
        String gunDisplayName = gunShopData.getGunDisplayName();

        String firstHologramLine = null;
        String secondHologramLine = null;

        if (gunShopData.isRequiresPower() && !isPowered()) {
            secondHologramLine = ChatColor.GRAY + "Requires Power!";
        } else {
            if (zombiesPlayer != null) {
                HotbarObjectGroup hotbarObjectGroup = zombiesPlayer.getHotbarManager()
                        .getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());
                if (hotbarObjectGroup != null) {
                    for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?> gun && gun.getEquipmentData().getName().equals(gunName)) {
                            firstHologramLine = String.format("%sRefill %s", ChatColor.GREEN, gunDisplayName);
                            secondHologramLine =
                                    String.format("%s%d Gold", ChatColor.GOLD, gunShopData.getRefillCost());
                            break;
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
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.player();
            if (!getShopData().isRequiresPower() || isPowered()) {
                HotbarManager hotbarManager = player.getHotbarManager();
                GunObjectGroup gunObjectGroup = (GunObjectGroup)
                        hotbarManager.getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());

                if (gunObjectGroup != null) {
                    Boolean refillAttempt = tryRefill(player, gunObjectGroup);
                    if (refillAttempt == null) {
                        if (tryBuy(player, gunObjectGroup)) {
                            player.getPlayer().playSound(Sound.sound(Key.key("block.note_block.pling"),
                                    Sound.Source.MASTER, 1.0F, 2.0F));
                            onPurchaseSuccess(player);
                            return true;
                        }
                    } else if (refillAttempt) {
                        player.getPlayer().playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER,
                                1.0F, 2.0F));
                        onPurchaseSuccess(player);
                        return true;
                    }
                } else {
                    player.getPlayer().sendMessage(Component.text("You cannot purchase guns in this map",
                            NamedTextColor.RED));
                }
            } else {
                player.getPlayer().sendMessage(Component.text("The power is not active yet!",
                        NamedTextColor.RED));
            }

            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER,
                    1.0F, 0.5F));
            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.GUN_SHOP.name();
    }

    /**
     * Attempts to refill a gun
     * @param player The player attempting to refill the gun
     * @param gunObjectGroup The gun object group in which the gun may reside
     * @return Whether purchase was successful, or null if no interaction occurred
     */
    private Boolean tryRefill(ZombiesPlayer player, GunObjectGroup gunObjectGroup) {
        GunShopData gunShopData = getShopData();

        for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<?, ?> gun
                    && gun.getEquipmentData().getName().equals(gunShopData.getGunName())) {
                if (gun.getCurrentAmmo() == gun.getCurrentLevel().getAmmo()) {
                    player.getPlayer().sendMessage(Component.text("Your gun is already filled!",
                            NamedTextColor.RED));
                    return false;
                } else {
                    int refillCost = gunShopData.getRefillCost();
                    if (player.getCoins() < refillCost) {
                        player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                                NamedTextColor.RED));
                        return false;
                    } else {
                        player.subtractCoins(refillCost);
                        gun.refill();
                        return true;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Attempts to purchase a gun
     * @param player The player receiving the gun
     * @param gunObjectGroup The gun object group that receives the gun
     * @return Whether purchase was successful
     */
    private boolean tryBuy(@NotNull ZombiesPlayer player, @NotNull GunObjectGroup gunObjectGroup) {
        GunShopData gunShopData = getShopData();

        Integer slot = gunObjectGroup.getNextEmptySlot();
        if (slot == null) {
            int selectedSlot = player.getPlayer().getInventory().getHeldItemSlot();
            if (gunObjectGroup.getHotbarObjectMap().containsKey(selectedSlot)) {
                slot = selectedSlot;
            } else {
                player.getPlayer().sendMessage(Component.text("Choose the slot you want to buy the gun in!",
                        NamedTextColor.RED));
                return false;
            }
        }

        int cost = gunShopData.getCost();
        if (player.getCoins() < cost) {
            player.getPlayer().sendMessage(Component.text("You cannot afford this item!", NamedTextColor.RED));
            return false;
        } else {
            player.subtractCoins(getShopData().getCost());
            gunObjectGroup.setHotbarObject(slot, equipmentManager.createEquipment(getArena(),
                    player, slot, map.getName(), gunShopData.getGunName()));

            return true;
        }

        return false;
    }

}
