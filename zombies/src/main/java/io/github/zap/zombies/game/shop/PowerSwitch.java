package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Switch used to turn on the power in the arena permanently
 */
public class PowerSwitch extends BlockShop<@NotNull PowerSwitchData> {

    public PowerSwitch(@NotNull World world, @NotNull ShopEventManager eventManager,
                       @NotNull PowerSwitchData shopData) {
        super(world, eventManager, shopData);
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
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.player();

            if (isPowered()) {
                player.getPlayer().sendMessage(Component.text("You have already turned on the power!",
                        NamedTextColor.RED));
            } else {
                int cost = getShopData().getCost();

                if (player.getCoins() < cost) {
                    player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                            NamedTextColor.RED));
                } else {
                    notifyPowerTurnedOn(player.getPlayer());

                    player.subtractCoins(cost);
                    onPurchaseSuccess(player);
                    return true;
                }
            }

            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER,
                    1.0F, 0.5F));

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
        for (Player playerInWorld : getWorld().getPlayers()) {
            playerInWorld.sendTitle(ChatColor.YELLOW + activator.getName() + " turned on the power!",
                    ChatColor.GOLD + "Shops which require power are now activated", 20, 60, 20);
            playerInWorld.playSound(Sound.sound(Key.key("minecraft:entity.blaze.death"),
                    Sound.Source.MASTER, 1.0F, 2.0F));
        }
    }
}
