package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.data.shop.BlockShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shop which interacts with a single block
 * @param <D> The data type of the shop
 */
public abstract class BlockShop<D extends @NotNull BlockShopData> extends Shop<@NotNull D> {

    @Getter
    private final Hologram hologram;

    @Getter
    private final Block block;

    public BlockShop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull D shopData) {
        super(world, eventManager, shopData);

        hologram = new Hologram(shopData.getHologramLocation().toLocation(world));
        block = world.getBlockAt(shopData.getBlockLocation().toLocation(world));
    }

    @Override
    public void onPlayerJoin(@NotNull List<@NotNull Player> players) {
        Hologram hologram = getHologram();

        for (Player player : players) {
            hologram.renderToPlayer(player);
        }

        super.onPlayerJoin(players);
    }

    @Override
    public void onPlayerRejoin(@NotNull List<? extends @NotNull ZombiesPlayer> players) {
        for (ZombiesPlayer player : players) {
            hologram.renderToPlayer(player.getPlayer());
        }

        super.onPlayerRejoin(players);
    }

    @Override
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (args.event() instanceof PlayerInteractEvent playerInteractEvent) {
            return block.equals(playerInteractEvent.getClickedBlock());
        }

        return false;
    }

}
