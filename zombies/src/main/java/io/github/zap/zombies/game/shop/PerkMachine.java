package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentCreator;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentDataManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.PerkMachineData;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.Perk;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Machine used to purchase or upgrade perks
 */
public class PerkMachine extends BlockShop<@NotNull PerkMachineData>  {

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull MapData map;

    private final @NotNull EquipmentDataManager equipmentDataManager;

    private final @NotNull EquipmentCreator equipmentCreator;

    public PerkMachine(@NotNull World world, @NotNull ShopEventManager eventManager,
                       @NotNull PerkMachineData shopData,
                       @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList, @NotNull MapData map,
                       @NotNull EquipmentDataManager equipmentDataManager, @NotNull EquipmentCreator equipmentCreator) {
        super(world, eventManager, shopData);

        this.playerList = playerList;
        this.map = map;
        this.equipmentDataManager = equipmentDataManager;
        this.equipmentCreator = equipmentCreator;
    }

    /**
     * Called when another perk machine is purchased from
     */
    public void onOtherPerkMachineUpdate(@NotNull ShopEventArgs<@NotNull PerkMachine, @NotNull ZombiesPlayer> args) {
        displayToPlayer(args.player().getPlayer());
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine("");
        }
        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        ZombiesPlayer zombiesPlayer = playerList.getOnlinePlayer(player);
        PerkMachineData perkMachineData = getShopData();
        Perk<?, ?, ?, ?> perk = determinePerk(zombiesPlayer);

        int level = (perk == null) ? 0 : perk.getLevel() + 1;

        String secondHologramLine;
        if (perk == null || level < perk.getEquipmentData().getLevels().size()) {
            secondHologramLine = perkMachineData.isRequiresPower() && !isPowered()
                    ? ChatColor.GRAY + "Requires Power!"
                    : String.format("%s%d Gold", ChatColor.GOLD, perkMachineData.getCosts().get(level));
        } else {
            secondHologramLine = ChatColor.GREEN + "Active";
        }


        Hologram hologram = getHologram();
        hologram.updateLineForPlayer(player, 0, String.format("%sBuy %s", ChatColor.BLUE,
                perkMachineData.getPerkName()));
        hologram.updateLineForPlayer(player, 1, secondHologramLine);
    }

    @Override
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.player();
                PerkMachineData perkMachineData = getShopData();
                Perk<@NotNull ?, @NotNull ?, ?, @NotNull ?> perk = determinePerk(player);

                if (!perkMachineData.isRequiresPower() || isPowered()) {
                    int level;
                    List<Integer> costs = perkMachineData.getCosts();

                    if (perk == null) {
                        if (!costs.isEmpty()) {
                            int cost = costs.get(0);

                            if (player.getCoins() < cost) {
                                player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                                        NamedTextColor.RED));
                            } else if (attemptToBuyPerk(player)) {
                                return true;
                            }
                        }
                    } else {
                        level = perk.getLevel() + 1;

                        if (level < costs.size()) {
                            int cost = costs.get(level);

                            if (player.getCoins() < cost) {
                                player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                                        NamedTextColor.RED));
                            } else {
                                player.subtractCoins(cost);
                                perk.upgrade();

                                onPurchaseSuccess(player);
                                return true;
                            }
                        } else {
                            player.getPlayer().sendMessage(Component.text("You have already maxed out this item!",
                                    NamedTextColor.RED));
                        }
                    }
                } else {
                    player.getPlayer().sendMessage(Component.text("The power is not active yet!",
                            NamedTextColor.RED));
                }

                player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                        Sound.Source.MASTER, 1.0F, 0.5F));
                return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.PERK_MACHINE.name();
    }

    /**
     * Finds the corresponding perk equipment within a player's hotbar
     * @param zombiesPlayer The player to search for the equipment in
     * @return The perk equipment, or null if it doesn't exist
     */
    private Perk<@NotNull ?, @NotNull ?, ?, @NotNull ?> determinePerk(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer != null) {
            EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup)
                    zombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());

            if (equipmentObjectGroup != null) {
                for (HotbarObject hotbarObject : equipmentObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof Perk<@NotNull ?, @NotNull ?, ?, @NotNull ?> perk &&
                            perk.getEquipmentData().getName().equals(getShopData().getPerkName())) {
                        return perk;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Attempts to purchase the perk for the first time for a player
     * @param player The purchasing player
     * @return Whether purchase was successful
     */
    private boolean attemptToBuyPerk(@NotNull ZombiesPlayer player) {
        HotbarManager hotbarManager = player.getHotbarManager();
        HotbarObjectGroup hotbarObjectGroup = hotbarManager
                .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
        if (hotbarObjectGroup != null) {
            Integer slot = hotbarObjectGroup.getNextEmptySlot();
            if (slot == null) {
                int heldSlot = player.getPlayer().getInventory().getHeldItemSlot();
                if (hotbarObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                    slot = heldSlot;
                }
            }

            if (slot != null) {
                EquipmentData<@NotNull ?> equipmentData = equipmentDataManager.getEquipmentData(map.getName(),
                        getShopData().getPerkName());

                if (equipmentData != null) {
                    Equipment<@NotNull ?, @NotNull ?> equipment = equipmentCreator.createEquipment(player, slot,
                            equipmentData);
                    if (equipment != null) {
                        if (equipment instanceof Perk<@NotNull ?, @NotNull ?, ?, @NotNull ?> perk) {
                            hotbarManager.setHotbarObject(slot, equipment);

                            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.firework_rocket.twinkle"),
                                    Sound.Source.MASTER, 1.0F, 1.0F));

                            player.subtractCoins(getShopData().getCosts().get(0));
                            onPurchaseSuccess(player);
                            return true;
                        }
                        else {
                            player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                                    NamedTextColor.RED));
                            Zombies.warning("Tried to give a player a gun with name " + getShopData().getPerkName()
                                    + " that isn't a gun!");
                        }
                    }
                    else {
                        player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                                NamedTextColor.RED));
                        Zombies.warning("Failed to create equipment with name " + getShopData().getPerkName() + "!");
                    }
                }
                else {
                    player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                            NamedTextColor.RED));
                    Zombies.warning("Failed to create equipment data with name " + getShopData().getPerkName() + "!");
                }
            } else {
                player.getPlayer().sendMessage(Component.text("Choose a slot to receive the perk in!",
                        NamedTextColor.RED));
            }
        } else {
            player.getPlayer().sendMessage(Component.text("You cannot receive this item!", NamedTextColor.RED));
        }

        return false;
    }

}
