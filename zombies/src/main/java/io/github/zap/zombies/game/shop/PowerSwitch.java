package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Locale;

/**
 * Switch used to turn on the power in the arena permanently
 */
public class PowerSwitch extends BlockShop<PowerSwitchData> {

    public PowerSwitch(ZombiesArena zombiesArena, PowerSwitchData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();

        LocalizationManager localizationManager = getLocalizationManager();
        hologram.setLine(
                0,
                ChatColor.GOLD.toString() + ChatColor.BOLD.toString()
                + localizationManager.getLocalizedMessageFor(player, MessageKey.POWER_SWITCH.getKey())
        );

        hologram.setLineFor(
                player,
                1,
                isPowered()
                        ? ChatColor.GREEN
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.ACTIVE.getKey())
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

            if (isPowered()) {
                localizationManager.sendLocalizedMessage(player, MessageKey.MAXED_OUT.getKey());
            } else {
                int cost = getShopData().getCost();

                if (zombiesPlayer.getCoins() < cost) {
                    localizationManager.sendLocalizedMessage(player, MessageKey.CANNOT_AFFORD.getKey());
                } else {
                    zombiesPlayer.subtractCoins(cost);

                    for (Player playerInWorld : getZombiesArena().getWorld().getPlayers()) {
                        Locale locale = localizationManager.getPlayerLocale(playerInWorld);
                        playerInWorld.sendTitle(
                                localizationManager
                                        .getLocalizedMessage(locale, MessageKey.ACTIVATED_POWER_TITLE.getKey()),
                                localizationManager
                                        .getLocalizedMessage(locale, MessageKey.ACTIVATED_POWER_SUBTITLE.getKey()),
                                20, 60, 20
                        );
                    }

                    onPurchaseSuccess(zombiesPlayer);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.POWER_SWITCH.name();
    }
}
