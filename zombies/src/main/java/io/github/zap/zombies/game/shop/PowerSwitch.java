package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PowerSwitch extends BlockShop<PowerSwitchData> {

    public PowerSwitch(ZombiesArena zombiesArena, PowerSwitchData shopData) {
        super(zombiesArena, shopData);

        // TODO: we can set the hologram location automatically but I'm banned so :/
    }

    @Override
    public void onOtherShopPurchase(String shopType) {
        super.onOtherShopPurchase(shopType);
        if (shopType.equals(getShopType())) {
            display();
        }
    }

    @Override
    public void displayTo(Player player) {
        Hologram hologram = getHologram();
        hologram.setLine(0, ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "Power Switch");

        hologram.setLine(1,
                isPowered() ?
                        ChatColor.GREEN + "ACTIVE" :
                        ChatColor.GOLD.toString() + getShopData().getCost() + " Gold"
        );
    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        if (isPowered()) {
            // TODO: already powered
        } else if (zombiesPlayer.getCoins() < getShopData().getCost()) {
            // TODO: poor
        } else {
            // TODO: success
            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.POWER_SWITCH.name();
    }
}
