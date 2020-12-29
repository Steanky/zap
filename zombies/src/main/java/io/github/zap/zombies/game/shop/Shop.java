package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@RequiredArgsConstructor
@Getter
public abstract class Shop<D extends ShopData> {

    private final ZombiesArena zombiesArena;

    private final D shopData;

    private boolean visible = false;

    private boolean powered = false;

    public void display(boolean firstTime) {
        for (Player player : zombiesArena.getWorld().getPlayers()) {
            displayTo(player, firstTime);
            visible = true;
        }
    }

    public void onOtherShopPurchase(String shopType) {
        if (shopType.equals(ShopType.POWER_SWITCH.name()) && shopData.isRequiresPower()) {
            powered = true;
            display(false);
        }
    }

    public abstract void displayTo(Player player, boolean firstTime);

    public abstract boolean purchase(ZombiesPlayer zombiesPlayer);

    public abstract boolean tryInteractWith(ZombiesArena.ProxyArgs<? extends Event> args);

    public abstract String getShopType();

}
