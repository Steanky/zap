package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ArmorStandShopData;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

@Getter
public abstract class ArmorStandShop<D extends ArmorStandShopData> extends Shop<D> {

    private final Hologram hologram;

    private final ArmorStand armorStand;

    public ArmorStandShop(ZombiesArena zombiesArena, D shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();
        BlockFace blockFace = getShopData().getBlockFace();
        Location location = getShopData().getBlockLocation().add(blockFace.getDirection()).toLocation(world);

        armorStand = world.spawn(location.clone().add(0.5, -1.0, 0.5), ArmorStand.class);
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        hologram = new Hologram(location.clone().add(0.5, -2.0, 0.5), 2);
    }

    @Override
    public boolean tryInteractWith(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractAtEntityEvent) {
            return armorStand.equals(((PlayerInteractAtEntityEvent) event).getRightClicked());
        }

        return false;
    }
}
