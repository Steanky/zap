package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.UltimateMachineData;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
            hologram.addLine(MessageKey.PLACEHOLDER.getKey());
        }
        super.display();
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();

        hologram.updateLineForPlayer(player, 0, MessageKey.ULTIMATE_MACHINE.getKey());

        hologram.updateLineForPlayer(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ImmutablePair.of(MessageKey.REQUIRES_POWER.getKey(), new String[]{})
                        : ImmutablePair.of(
                                MessageKey.COST.toString(), new String[]{ String.valueOf(getShopData().getCost()) }
                                )
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
    public ShopType getShopType() {
        return ShopType.ULTIMATE_MACHINE;
    }
}
