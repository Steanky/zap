package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.UltimateMachineData;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Machine for upgrading designated upgradeable equipment
 */
public class UltimateMachine extends BlockShop<UltimateMachineData> {

    public UltimateMachine(ZombiesArena zombiesArena, UltimateMachineData shopData) {
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
        Hologram hologram = getHologram();

        hologram.updateLineForPlayer(player, 0, ChatColor.GOLD + "Ultimate Machine");

        hologram.updateLineForPlayer(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY + "Requires Power!"
                        : String.format("%s%d Gold", ChatColor.GOLD, getShopData().getCost())
        );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                    player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                } else {
                    HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();

                    HotbarObject hotbarObject = hotbarManager.getSelectedObject();
                    if (hotbarObject instanceof Ultimateable && hotbarObject instanceof UpgradeableEquipment<?, ?>) {
                        UpgradeableEquipment<?, ?> upgradeableEquipment = (UpgradeableEquipment<?, ?>) hotbarObject;
                        if (upgradeableEquipment.getLevel()
                                < upgradeableEquipment.getEquipmentData().getLevels().size()) {

                            upgradeableEquipment.upgrade();
                            onPurchaseSuccess(zombiesPlayer);
                        } else {
                            player.sendMessage(ChatColor.RED + "You have already maxed out this item!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Choose a slot to receive this item in!");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "The power is not active yet!");
            }

            return true;
        }
        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.ULTIMATE_MACHINE;
    }
}
