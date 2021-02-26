package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ArmorStandShopData;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * Shop which interacts with a single invisible armor stand
 * @param <D> The data type of the shop
 */
@Getter
public abstract class ArmorStandShop<D extends ArmorStandShopData> extends Shop<D> {

    private final Hologram hologram;

    private final ArmorStand armorStand;

    public ArmorStandShop(ZombiesArena zombiesArena, D shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();

        armorStand = world.spawn(
                getShopData().getRootLocation().toLocation(world).add(0.5, -1.0, 0.5),
                ArmorStand.class
        );
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        hologram = new Hologram(
                getShopData().getHologramLocation().toLocation(world).add(0.5, -2.0, 0.5),
                2
        );
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        Hologram hologram = getHologram();
        for (Player player : args.getPlayers()) {
            hologram.renderTo(player);
        }

        super.onPlayerJoin(args);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractAtEntityEvent) {
            PlayerInteractAtEntityEvent playerInteractAtEntityEvent = (PlayerInteractAtEntityEvent) event;
            return playerInteractAtEntityEvent.getRightClicked().equals(armorStand);
        }

        return false;
    }
}
