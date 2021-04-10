package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PerkMachineData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.Perk;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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
    protected void displayToPlayer(Player player) {
        ZombiesPlayer zombiesPlayer = getZombiesArena().getPlayerMap().get(player.getUniqueId());
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

            if (zombiesPlayer != null) {
                Player player = zombiesPlayer.getPlayer();

                if (player != null) {
                    PerkMachineData perkMachineData = getShopData();
                    Perk<?, ?, ?, ?> perk = determinePerk(zombiesPlayer);

                    if (!perkMachineData.isRequiresPower() || isPowered()) {
                        int level;
                        List<Integer> costs = perkMachineData.getCosts();

                        if (perk == null) {
                            if (costs.size() != 0) {
                                int cost = costs.get(0);

                                if (zombiesPlayer.getCoins() < cost) {
                                    player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                                } else {
                                    HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                                    HotbarObjectGroup hotbarObjectGroup = hotbarManager
                                            .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
                                    if (hotbarObjectGroup != null) {
                                        Integer slot = hotbarObjectGroup.getNextEmptySlot();
                                        if (slot == null) {
                                            int heldSlot = player.getInventory().getHeldItemSlot();
                                            if (hotbarObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                                                slot = heldSlot;
                                            }
                                        }

                                        if (slot != null) {
                                            ZombiesArena zombiesArena = getZombiesArena();
                                            hotbarManager.setHotbarObject(slot, zombiesArena.getEquipmentManager()
                                                    .createEquipment(zombiesArena, zombiesPlayer, slot,
                                                            zombiesArena.getMap().getName(),
                                                            perkMachineData.getPerkName()));

                                            player.playSound(Sound.sound(
                                                    Key.key("minecraft:entity.firework_rocket.twinkle"),
                                                    Sound.Source.MASTER, 1.0F, 1.0F));

                                            zombiesPlayer.subtractCoins(cost);
                                            onPurchaseSuccess(zombiesPlayer);

                                            return true;
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Choose a slot to receive the perk in!");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You cannot receive this item!");
                                    }
                                }
                            }
                        } else {
                            level = perk.getLevel() + 1;

                            if (level < costs.size()) {
                                int cost = costs.get(level);

                                if (zombiesPlayer.getCoins() < cost) {
                                    player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                                } else {
                                    zombiesPlayer.subtractCoins(cost);
                                    perk.upgrade();

                                    onPurchaseSuccess(zombiesPlayer);

                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You have already maxed out this item!");
                            }
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
                }
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
    private Perk<?, ?, ?, ?> determinePerk(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer != null) {
            EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup)
                    zombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
            if (equipmentObjectGroup != null) {
                for (HotbarObject hotbarObject : equipmentObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof Perk) {
                        Perk<?, ?, ?, ?> perk = (Perk<?, ?, ?, ?>) hotbarObject;

                        if (perk.getEquipmentData().getPerkType().equals(getShopData().getPerkType())) {
                            return perk;
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
