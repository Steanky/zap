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
import org.jetbrains.annotations.NotNull;

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
            ZombiesPlayer player = args.getManagedPlayer();

            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    if (isPowered()) {
                        bukkitPlayer.sendMessage(ChatColor.RED + "You have already turned on the power!");
                    } else {
                        int cost = getShopData().getCost();

                        if (player.getCoins() < cost) {
                            bukkitPlayer.sendMessage(ChatColor.RED + "You cannot afford this item!");
                        } else {
                            notifyPowerTurnedOn(bukkitPlayer);

                            player.subtractCoins(cost);
                            onPurchaseSuccess(player);

                            return true;
                        }
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER,
                            1.0F, 0.5F));
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

    /**
     * Notifies all players in the world that the power is turned on
     * @param activator The player that turned on the power
     */
    private void notifyPowerTurnedOn(@NotNull Player activator) {
        for (Player playerInWorld : getArena().getWorld().getPlayers()) {
            playerInWorld.sendTitle(ChatColor.YELLOW + activator.getName() + " turned on the power!",
                    ChatColor.GOLD + "Shops which require power are now activated", 20, 60, 20);
            playerInWorld.playSound(Sound.sound(Key.key("minecraft:entity.blaze.death"),
                    Sound.Source.MASTER, 1.0F, 2.0F));
        }
    }
}
