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

public abstract class BlockShop<D extends BlockShopData> extends Shop<D> {

    @Getter
    private final Hologram hologram;

    private final Block block;

    public BlockShop(ZombiesArena zombiesArena, D shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();

        hologram = new Hologram(shopData.getHologramLocation().toLocation(world), 2);
        block = world.getBlockAt(shopData.getBlockLocation().toLocation(world));
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        Hologram hologram = getHologram();
        for (Player player : args.getPlayers()) {
            hologram.renderTo(player);
            displayTo(player);
        }
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        return (
                event instanceof PlayerInteractEvent
                &&
                block.equals(((PlayerInteractEvent) event).getClickedBlock())
        );
    }
}
