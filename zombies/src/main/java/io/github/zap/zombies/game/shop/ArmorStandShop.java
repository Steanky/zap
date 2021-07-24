package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.data.shop.ArmorStandShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shop which interacts with a single invisible armor stand
 * @param <D> The data type of the shop
 */
@Getter
public abstract class ArmorStandShop<D extends @NotNull ArmorStandShopData> extends Shop<@NotNull D> {

    private final Hologram hologram;

    private final ArmorStand armorStand;

    public ArmorStandShop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull D shopData) {
        super(world, eventManager, shopData);

        armorStand = world.spawn(getShopData().getRootLocation().toLocation(world).add(0.5D, -1.0D, 0.5D),
                ArmorStand.class);
        armorStand.setCollidable(false);
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        hologram = new Hologram(getShopData().getRootLocation().toLocation(world).add(0.5D, -2.0D, 0.5D));
    }

    @Override
    public void onPlayerJoin(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            hologram.renderToPlayer(player);
        }

        super.onPlayerJoin(players);
    }

    @Override
    public void onPlayerRejoin(@NotNull List<? extends @NotNull ZombiesPlayer> players) {
        Hologram hologram = getHologram();

        for (ZombiesPlayer player : players) {
            hologram.renderToPlayer(player.getPlayer());
        }

        super.onPlayerRejoin(players);
    }

    @Override
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (args.event() instanceof PlayerInteractAtEntityEvent event) {
            return event.getRightClicked().equals(armorStand);
        }

        return false;
    }

}
