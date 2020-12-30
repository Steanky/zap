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

public class PerkMachine extends BlockShop<PerkMachineData>  {

    public PerkMachine(ZombiesArena zombiesArena, PerkMachineData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();

        hologram.setLineFor(
                player,
                0,
                ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + getShopData().getPerkType().name()
        );

        ZombiesPlayer zombiesPlayer = getZombiesArena().getPlayerMap().get(player.getUniqueId());
        PerkEquipment perkEquipment = determinePerkEquipment(zombiesPlayer);

        int level = (perkEquipment == null) ? 0 : perkEquipment.getLevel() + 1;

        if (perkEquipment == null || level < perkEquipment.getEquipmentData().getLevels().size()) {
            hologram.setLineFor(player, 1,
                    getShopData().isRequiresPower() && !isPowered()
                            ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!"
                            : ChatColor.GOLD.toString() + getShopData().getCosts().get(level) + " Gold"
            );
        } else {
            hologram.setLineFor(player, 1,ChatColor.GREEN + "UNLOCKED");
        }
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            PerkEquipment perkEquipment = determinePerkEquipment(zombiesPlayer);

            int level;
            List<Integer> costs = getShopData().getCosts();

            if (perkEquipment == null) {
                if (costs.size() != 0) {
                    int cost = costs.get(0);

                    if (zombiesPlayer.getCoins() < cost) {
                        HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                        PerkObjectGroup perkObjectGroup =
                                (PerkObjectGroup) hotbarManager.getHotbarObjectGroup(EquipmentType.PERK.name());
                        if (perkObjectGroup != null) {
                            Integer slot = perkObjectGroup.getNextEmptySlot();
                            if (slot != null) {
                                hotbarManager.setHotbarObject(slot, getZombiesArena().getEquipmentManager()
                                .createEquipment(
                                        zombiesPlayer.getPlayer(),
                                        slot,
                                        getZombiesArena().getMap().getMapNameKey(),
                                        getShopData().getPerkType().name()
                                ));
                            } else {
                                // TODO: choose a slot
                            }
                        } else {
                            // TODO: can't buy perks
                        }
                    } else {
                        // TODO: poor
                    }
                }
            } else {
                level = perkEquipment.getLevel() + 1;

                if (level < costs.size()) {
                    int cost = costs.get(level);
                    if (zombiesPlayer.getCoins() < cost) {
                        zombiesPlayer.subtractCoins(cost);
                        perkEquipment.upgrade();
                    } else {
                        // TODO: poor
                    }
                } else {
                    // TODO: unlocked
                }
            }

            return true;
        }

        return false;
    }

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
