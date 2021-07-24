package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentCreator;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentDataManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.shop.GunShopData;
import io.github.zap.zombies.game.equipment.Equipment;
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

    private final @NotNull EquipmentDataManager equipmentDataManager;

    private final @NotNull EquipmentCreator equipmentCreator;

    private final @NotNull Set<@NotNull Item> protectedItems;

    private Item item = null;

    public GunShop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull GunShopData shopData,
                   @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList, @NotNull MapData map,
                   @NotNull EquipmentDataManager equipmentDataManager, @NotNull EquipmentCreator equipmentCreator,
                   @NotNull Set<@NotNull Item> protectedItems) {
        super(world, eventManager, shopData);

        this.playerList = playerList;
        this.map = map;
        this.equipmentDataManager = equipmentDataManager;
        this.equipmentCreator = equipmentCreator;
        this.protectedItems = protectedItems;
    }

    /**
     * Called when another gun shop is purchased from
     */
    public void onOtherGunShopUpdate(@NotNull ShopEventArgs<@NotNull ArmorShop, @NotNull ZombiesPlayer> args) {
        displayToPlayer(args.player().getPlayer());
    }

    /**
     * Called when a {@link LuckyChest} is purchased from
     */
    public void onLuckyChestUpdate(@NotNull ShopEventArgs<@NotNull LuckyChest, @NotNull ZombiesPlayer> args) {
        displayToPlayer(args.player().getPlayer());
    }

    /**
     * Called when a {@link PiglinShop} is purchased from
     */
    public void onPiglinShopUpdate(@NotNull ShopEventArgs<@NotNull PiglinShop, @NotNull ZombiesPlayer> args) {
        displayToPlayer(args.player().getPlayer());
    }

    @Override
    public void display() {
        if (item == null) {
            World world = getWorld();

            EquipmentData<@NotNull ?> equipmentData = equipmentDataManager.getEquipmentData(map.getName(),
                    getShopData().getGunName());

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
                        if (hotbarObject instanceof Gun<@NotNull ?, @NotNull ?> gun && gun.getEquipmentData().getName()
                                .equals(gunName)) {
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

        int cost = getShopData().getCost();
        if (player.getCoins() < cost) {
            player.getPlayer().sendMessage(Component.text("You cannot afford this item!", NamedTextColor.RED));
        } else {
            EquipmentData<@NotNull ?> equipmentData = equipmentDataManager.getEquipmentData(map.getName(),
                    getShopData().getGunName());
            if (equipmentData != null) {
                Equipment<@NotNull ?, @NotNull ?> equipment = equipmentCreator.createEquipment(player, slot,
                        equipmentData);
                if (equipment != null) {
                    if (equipment instanceof Gun<@NotNull ?, @NotNull ?>) {
                        gunObjectGroup.setHotbarObject(slot, equipment);
                        player.subtractCoins(getShopData().getCost());
                        return true;
                    }
                    else {
                        Zombies.warning("Tried to give a player a gun with name " + getShopData().getGunName()
                                + " that isn't a gun!");
                        player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                                NamedTextColor.RED));
                    }
                }
                else {
                    Zombies.warning("Failed to create equipment with name " + getShopData().getGunName() + "!");
                    player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                            NamedTextColor.RED));
                }
            }
            else {
                Zombies.warning("Failed to create equipment data with name " + getShopData().getGunName() + "!");
                player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                        NamedTextColor.RED));
            }
        }

        return false;
    }

}
