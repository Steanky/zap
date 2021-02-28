package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Switch used to turn on the power in the arena permanently
 */
public class PowerSwitch extends BlockShop<PowerSwitchData> {

    public PowerSwitch(ZombiesArena zombiesArena, PowerSwitchData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine(MessageKey.PLACEHOLDER.getKey());
        }

        hologram.updateLineForEveryone(0, MessageKey.POWER_SWITCH.getKey());
        hologram.updateLineForEveryone(1,
                isPowered()
                        ? ImmutablePair.of(MessageKey.ACTIVE.getKey(), new String[]{})
                        : ImmutablePair.of(
                                MessageKey.COST.getKey(),
                        new String[]{ String.valueOf(getShopData().getCost()) }
                        )
                );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            LocalizationManager localizationManager = getLocalizationManager();
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();

            if (isPowered()) {
                player.sendMessage(ChatColor.RED + "You have already turned on the power!");
            } else {
                int cost = getShopData().getCost();

                if (zombiesPlayer.getCoins() < cost) {
                    player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                } else {
                    zombiesPlayer.subtractCoins(cost);

                    for (Player playerInWorld : getZombiesArena().getWorld().getPlayers()) {
                        playerInWorld.sendTitle(
                                ChatColor.YELLOW + "Somebody turned on the power!",
                                ChatColor.GOLD + "what should I put here?",
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
    public ShopType getShopType() {
        return ShopType.POWER_SWITCH;
    }
}
