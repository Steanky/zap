package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
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
    protected void displayTo(Player player) {
        ZombiesPlayer zombiesPlayer = getZombiesArena().getPlayerMap().get(player.getUniqueId());
        PerkMachineData perkMachineData = getShopData();
        PerkEquipment perkEquipment = determinePerkEquipment(zombiesPlayer);

        int level = (perkEquipment == null) ? 0 : perkEquipment.getLevel() + 1;

        String secondHologramLine;
        if (perkEquipment == null || level < perkEquipment.getEquipmentData().getLevels().size()) {
            secondHologramLine = perkMachineData.isRequiresPower() && !isPowered()
                    ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!"
                    : ChatColor.GOLD.toString() + perkMachineData.getCosts().get(level) + " Gold";
        } else {
            secondHologramLine = ChatColor.GREEN + "ACTIVE";
        }


        Hologram hologram = getHologram();
        hologram.setLineFor(
                player,
                0,
                ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + perkMachineData.getPerkType().name()
        );
        hologram.setLineFor(player, 1, secondHologramLine);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            LocalizationManager localizationManager = getLocalizationManager();
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
                            localizationManager.sendLocalizedMessage(player,
                                    ChatColor.RED + MessageKey.CANNOT_AFFORD.getKey());
                        } else {
                            HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                            PerkObjectGroup perkObjectGroup =
                                    (PerkObjectGroup) hotbarManager.getHotbarObjectGroup(EquipmentType.PERK.name());
                            if (perkObjectGroup != null) {
                                Integer slot = perkObjectGroup.getNextEmptySlot();
                                if (slot != null) {
                                    zombiesPlayer.getPerks(); // TODO: adding perks

                                    ZombiesArena zombiesArena = getZombiesArena();
                                    hotbarManager.setHotbarObject(slot, zombiesArena.getEquipmentManager()
                                            .createEquipment(
                                                    zombiesPlayer.getPlayer(),
                                                    slot,
                                                    zombiesArena.getMap().getMapNameKey(),
                                                    perkMachineData.getPerkType().name()
                                            ));

                                    onPurchaseSuccess(zombiesPlayer);
                                } else {
                                    // We already know the player doesn't have the equipment
                                    // TODO: choose a slot
                                }
                            } else {
                                // TODO: can't buy perks
                            }
                        }
                    } else {
                        // TODO: you can't buy anything!
                    }
                } else {
                    level = perkEquipment.getLevel() + 1;

                    if (level < costs.size()) {
                        int cost = costs.get(level);

                        if (zombiesPlayer.getCoins() < cost) {
                            localizationManager.sendLocalizedMessage(player,
                                    ChatColor.RED + MessageKey.CANNOT_AFFORD.getKey());
                        } else {
                            zombiesPlayer.subtractCoins(cost);
                            perkEquipment.upgrade();

                            onPurchaseSuccess(zombiesPlayer);
                        }
                    } else {
                        // TODO: unlocked
                    }
                }
            } else {
                localizationManager.sendLocalizedMessage(player,
                        ChatColor.RED + MessageKey.NO_POWER.getKey());
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
    public String getShopType() {
        return ShopType.PERK_MACHINE.name();
    }
}
