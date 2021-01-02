package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.DoorSide;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a door used to open other rooms
 */
public class Door extends Shop<DoorData> {

    private final Map<DoorSide, Hologram> doorSideHologramMap = new HashMap<>();

    private boolean opened = false;

    public Door(ZombiesArena zombiesArena, DoorData shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();
        for (DoorSide doorSide : getShopData().getDoorSides()) {
            doorSideHologramMap.put(
                    doorSide,
                    new Hologram(doorSide.getHologramLocation().toLocation(world), 2)
            );
        }
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();
    }

    @Override
    protected void displayTo(Player player) {
        if (!opened) {
            for (Map.Entry<DoorSide, Hologram> entry : doorSideHologramMap.entrySet()) {
                Hologram hologram = entry.getValue();
                hologram.renderTo(player);

                LocalizationManager localizationManager = getLocalizationManager();
                StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN.toString());
                List<String> opensTo = entry.getKey().getOpensTo();
                if (opensTo.size() > 0) {
                    stringBuilder.append(
                            localizationManager.getLocalizedMessageFor(player, opensTo.get(0))
                    );
                    for (int i = 1; i < opensTo.size(); i++) {
                        stringBuilder.append(" & ");
                        stringBuilder.append(
                                localizationManager.getLocalizedMessageFor(player, opensTo.get(i))
                        );
                    }
                }

                hologram.setLineFor(player, 0, stringBuilder.toString());
                hologram.setLineFor(
                        player,
                        1,
                        ChatColor.GOLD.toString() + entry.getKey().getCost() + " "
                        + localizationManager.getLocalizedMessageFor(player, MessageKey.GOLD.getKey())
                );
            }
        }
    }

    @Override
    public boolean purchase(ManagingArena<ZombiesArena, ZombiesPlayer>.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractEvent) {
            LocalizationManager localizationManager = getLocalizationManager();
            DoorData doorData = getShopData();
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();

            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) args.getEvent();
            Block block = playerInteractEvent.getClickedBlock();

            if (block != null && doorData.getDoorBounds().contains(block.getLocation().toVector())) {
                for (DoorSide doorSide : doorData.getDoorSides()) {
                    if (doorSide.getTriggerBounds().contains(player.getLocation().toVector())) {
                        int cost = doorSide.getCost();
                        if (zombiesPlayer.getCoins() < cost) {
                            localizationManager.sendLocalizedMessage(player, MessageKey.CANNOT_AFFORD.toString());
                        } else {
                            ZombiesArena zombiesArena = getZombiesArena();
                            WorldUtils.fillBounds(
                                    zombiesArena.getWorld(),
                                    doorData.getDoorBounds(),
                                    zombiesArena.getMap().getDoorFillMaterial()
                            );
                            zombiesPlayer.subtractCoins(cost);

                            for (Hologram hologram : doorSideHologramMap.values()) {
                                hologram.destroy();
                            }
                            opened = true;

                            onPurchaseSuccess(zombiesPlayer);
                        }

                        break;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.DOOR.name();
    }
}