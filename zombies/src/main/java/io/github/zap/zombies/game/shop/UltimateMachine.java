package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.HologramReplacement;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
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
    protected void displayTo(Player player) {
        HologramReplacement hologram = getHologram();

        LocalizationManager localizationManager = getLocalizationManager();
        hologram.updateLineForPlayer(player, 0, ChatColor.GREEN.toString() + ChatColor.BOLD.toString()
                + localizationManager.getLocalizedMessageFor(player, MessageKey.ULTIMATE_MACHINE.getKey()));

        hologram.updateLineForPlayer(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.REQUIRES_POWER.getKey())
                        : ChatColor.GOLD.toString() + getShopData().getCost() + " "
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.GOLD.getKey())
        );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            LocalizationManager localizationManager = getLocalizationManager();
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                    localizationManager.sendLocalizedMessage(player, MessageKey.CANNOT_AFFORD.getKey());
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
                            localizationManager.sendLocalizedMessage(player, MessageKey.MAXED_OUT.getKey());
                        }
                    } else {
                        localizationManager.sendLocalizedMessage(player, MessageKey.CHOOSE_SLOT.getKey());
                    }
                }
            } else {
                localizationManager.sendLocalizedMessage(player, MessageKey.NO_POWER.getKey());
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
