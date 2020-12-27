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

public class UltimateMachine extends BlockShop<UltimateMachineData> {

    public UltimateMachine(ZombiesArena zombiesArena, UltimateMachineData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    public void displayTo(Player player) {
        Hologram hologram = getHologram();
        hologram.setLine(0, ChatColor.GREEN + "Ultimate Machine");

        hologram.setLine(1,
                isPowered() ?
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Requires Power!" :
                        ChatColor.GOLD.toString() + getShopData().getCost() + " Gold"
        );
    }

    @Override
    public Boolean purchase(ZombiesPlayer zombiesPlayer) {
        if (isPowered()) {
            if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                // TODO: poor
            } else {
                HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                HotbarObject hotbarObject = hotbarManager.getSelectedObject();
                if (hotbarObject instanceof UpgradeableEquipment<?, ?>) {
                    UpgradeableEquipment<?, ?> upgradeableEquipment = (UpgradeableEquipment<?, ?>) hotbarObject;
                    if (upgradeableEquipment.getLevel() < upgradeableEquipment.getEquipmentData().getLevels().size()) {
                        upgradeableEquipment.upgrade();
                        // TODO: send confirmation messages
                        return true;
                    } else {
                        // TODO: max level
                    }
                } else {
                    // TODO: swap to an upgradeable weapon
                }
            }
        } else {
            // TODO: needs powa
        }
        return false;
    }
}
