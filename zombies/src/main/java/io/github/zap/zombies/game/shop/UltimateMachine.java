package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.UltimateMachineData;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class UltimateMachine extends BlockShop<UltimateMachineData> {

    public UltimateMachine(ZombiesArena zombiesArena, UltimateMachineData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();

        hologram.setLineFor(player, 0, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Ultimate Machine");

        hologram.setLineFor(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!"
                        : ChatColor.GOLD.toString() + getShopData().getCost() + " Gold"
        );
    }

    @Override
    protected boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                    // TODO: poor
                } else {
                    HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();

                    HotbarObject hotbarObject = hotbarManager.getSelectedObject();
                    if (hotbarObject instanceof UpgradeableEquipment<?, ?>) {
                        UpgradeableEquipment<?, ?> upgradeableEquipment = (UpgradeableEquipment<?, ?>) hotbarObject;
                        if (upgradeableEquipment.getLevel()
                                < upgradeableEquipment.getEquipmentData().getLevels().size()) {

                            upgradeableEquipment.upgrade();
                            onPurchaseSuccess(zombiesPlayer);
                        } else {
                            // TODO: max level
                        }
                    } else {
                        // TODO: swap to an upgradeable weapon
                    }
                }
            } else {
                // TODO: needs power
            }

            return true;
        }
        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.ULTIMATE_MACHINE.name();
    }
}
