package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.BlockShopData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Shop which interacts with a single block
 * @param <D> The data type of the shop
 */
public abstract class BlockShop<D extends BlockShopData> extends Shop<D> {

    @Getter
    private final Hologram hologram;

    private final Block block;

    public BlockShop(ZombiesArena zombiesArena, D shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();

        hologram = new Hologram(shopData.getHologramLocation().toLocation(world));
        block = world.getBlockAt(shopData.getBlockLocation().toLocation(world));
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        Hologram hologram = getHologram();
        for (Player player : args.getPlayers()) {
            hologram.renderToPlayer(player);
        }
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        return block.equals(((PlayerInteractEvent) args.getEvent()).getClickedBlock());
    }

}
