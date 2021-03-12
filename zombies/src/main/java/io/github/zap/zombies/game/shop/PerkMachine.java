package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PerkMachineData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.perk.PerkEquipment;
import io.github.zap.zombies.game.equipment.perk.PerkObjectGroup;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

/**
 * Machine used to purchase or upgrade perks
 */
public class PerkMachine extends BlockShop<PerkMachineData>  {

    public PerkMachine(ZombiesArena zombiesArena, PerkMachineData shopData) {
        super(zombiesArena, shopData);
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
    protected void displayTo(Player player) {
        ZombiesPlayer zombiesPlayer = getZombiesArena().getPlayerMap().get(player.getUniqueId());
        PerkMachineData perkMachineData = getShopData();
        PerkEquipment perkEquipment = determinePerkEquipment(zombiesPlayer);

        int level = (perkEquipment == null) ? 0 : perkEquipment.getLevel() + 1;

        String secondHologramLine;
        if (perkEquipment == null || level < perkEquipment.getEquipmentData().getLevels().size()) {
            secondHologramLine = perkMachineData.isRequiresPower() && !isPowered()
                    ? ChatColor.GRAY + "Requires Power!"
                    : String.format("%s%d Gold", ChatColor.GOLD, perkMachineData.getCosts().get(level));
        } else {
            secondHologramLine = ChatColor.GREEN + "Active";
        }


        Hologram hologram = getHologram();
        hologram.updateLineForPlayer(
                player,
                0,
                String.format("%sBuy %s", ChatColor.BLUE, perkMachineData.getPerkName())
        );
        hologram.updateLineForPlayer(player, 1, secondHologramLine);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();
            PerkMachineData perkMachineData = getShopData();
            PerkEquipment perkEquipment = determinePerkEquipment(zombiesPlayer);

            if (!perkMachineData.isRequiresPower() || isPowered()) {
                int level;
                List<Integer> costs = perkMachineData.getCosts();

                if (perkEquipment == null) {
                    if (costs.size() != 0) {
                        int cost = costs.get(0);

                        if (zombiesPlayer.getCoins() < cost) {
                            player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                        } else {
                            HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                            PerkObjectGroup perkObjectGroup =
                                    (PerkObjectGroup) hotbarManager.getHotbarObjectGroup(EquipmentType.PERK.name());
                            if (perkObjectGroup != null) {
                                Integer slot = perkObjectGroup.getNextEmptySlot();
                                if (slot == null) {
                                    int heldSlot = player.getInventory().getHeldItemSlot();
                                    if (perkObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                                        slot = heldSlot;
                                    }
                                }
                                if (slot != null) {
                                    zombiesPlayer.getPerks().getPerk(perkMachineData.getPerkType()).upgrade();

                                    ZombiesArena zombiesArena = getZombiesArena();
                                    hotbarManager.setHotbarObject(slot, zombiesArena.getEquipmentManager()
                                            .createEquipment(
                                                    zombiesArena,
                                                    zombiesPlayer,
                                                    slot,
                                                    zombiesArena.getMap().getName(),
                                                    perkMachineData.getPerkName()
                                            ));

                                    onPurchaseSuccess(zombiesPlayer);
                                } else {
                                    player.sendMessage(ChatColor.RED + "Choose a slot to receive the perk in!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot receive this item!");
                            }
                        }
                    }
                } else {
                    level = perkEquipment.getLevel() + 1;

                    if (level < costs.size()) {
                        int cost = costs.get(level);

                        if (zombiesPlayer.getCoins() < cost) {
                            player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                        } else {
                            zombiesPlayer.subtractCoins(cost);
                            perkEquipment.upgrade();
                            zombiesPlayer.getPerks().getPerk(perkMachineData.getPerkType()).upgrade();

                            onPurchaseSuccess(zombiesPlayer);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You have already maxed out this item!");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "The power is not active yet!");
            }

            return true;
        }

        return false;
    }

    /**
     * Finds the corresponding perk equipment within a player's hotbar
     * @param zombiesPlayer The player to search for the equipment in
     * @return The perk equipment, or null if it doesn't exist
     */
    private PerkEquipment determinePerkEquipment(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer != null) {
            EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup)
                    zombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentType.PERK.name());
            if (equipmentObjectGroup != null) {
                for (HotbarObject hotbarObject : equipmentObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof PerkEquipment) {
                        PerkEquipment perkEquipment = (PerkEquipment) hotbarObject;

                        if (perkEquipment.getEquipmentData().getPerkType().equals(getShopData().getPerkType())) {
                            return perkEquipment;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.PERK_MACHINE;
    }
}
