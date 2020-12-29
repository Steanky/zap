package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class PowerSwitch extends BlockShop<PowerSwitchData> {

    public PowerSwitch(ZombiesArena zombiesArena, PowerSwitchData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    public void displayTo(Player player) {
        Hologram hologram = getHologram();

        hologram.setLine(0, ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "Power Switch");

        hologram.setLine(1,
                isPowered()
                        ? ChatColor.GREEN + "ACTIVE"
                        : ChatColor.GOLD.toString() + getShopData().getCost() + " Gold"
        );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            if (isPowered()) {
                // TODO: already powered
            } else if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                // TODO: poor
            } else {
                onPurchaseSuccess(zombiesPlayer);
                // TODO: success
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
