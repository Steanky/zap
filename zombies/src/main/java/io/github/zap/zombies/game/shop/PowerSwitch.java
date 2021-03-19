package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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
            hologram.addLine("");
        }

        hologram.updateLineForEveryone(0, ChatColor.GOLD + "Power Switch");
        hologram.updateLineForEveryone(1,
                isPowered()
                        ? ChatColor.GREEN + "Active"
                        : String.format("%s%d Gold", ChatColor.GOLD, getShopData().getCost())
                );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();

            if (zombiesPlayer != null) {
                Player player = zombiesPlayer.getPlayer();

                if (player != null) {
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
                                        ChatColor.YELLOW + player.getName() + " turned on the power!",
                                        ChatColor.GOLD + "Shops which require power are now activated",
                                        20, 60, 20
                                );
                                playerInWorld.playSound(Sound.sound(
                                        Key.key("minecraft:entity.blaze.death"),
                                        Sound.Source.MASTER,
                                        1.0F,
                                        2.0F
                                ));
                            }

                            onPurchaseSuccess(zombiesPlayer);
                            return true;
                        }
                    }

                    player.playSound(Sound.sound(
                            Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER,
                            1.0F,
                            0.5F
                    ));

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.POWER_SWITCH;
    }
}
