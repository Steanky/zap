package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.DoorSide;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Duration;
import java.util.ArrayList;
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
            Hologram hologram = new Hologram(doorSide.getHologramLocation().toLocation(world));
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine("");
            }

            doorSideHologramMap.put(doorSide, hologram);
        }
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        for (Hologram hologram : doorSideHologramMap.values()) {
            for (Player player : args.getPlayers()) {
                hologram.renderToPlayer(player);
            }
        }

        super.onPlayerJoin(args);
    }

    @Override
    public void display() {
        if (!opened) {
            for (Map.Entry<DoorSide, Hologram> entry : doorSideHologramMap.entrySet()) {
                Hologram hologram = entry.getValue();

                StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN.toString());
                List<String> opensTo = entry.getKey().getOpensTo();
                if (opensTo.size() > 0) {
                    stringBuilder.append(
                            opensTo.get(0)
                    );
                    for (int i = 1; i < opensTo.size(); i++) {
                        stringBuilder.append(" & ");
                        stringBuilder.append(
                                opensTo.get(i)
                        );
                    }
                }

                hologram.updateLineForEveryone(0, stringBuilder.toString());
                hologram.updateLineForEveryone(
                        1,
                        ChatColor.GOLD.toString() + entry.getKey().getCost() + " Gold"
                );
            }
        }
    }

    @Override
    public boolean purchase(ManagingArena<ZombiesArena, ZombiesPlayer>.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractEvent) {
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
                            player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                        } else {
                            ZombiesArena zombiesArena = getZombiesArena();
                            WorldUtils.fillBounds(
                                    zombiesArena.getWorld(),
                                    doorData.getDoorBounds(),
                                    zombiesArena.getMap().getDoorFillMaterial()
                            );
                            Location playerLoc = player.getLocation();
                            zombiesArena.getWorld().playSound(doorData.getOpenSound(), playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());
                            zombiesPlayer.subtractCoins(cost);

                            List<String> newlyOpened = new ArrayList<>();
                            for(String openedRoom : doorSide.getOpensTo()) {
                                RoomData room = zombiesArena.getMap().getNamedRoom(openedRoom);
                                Property<Boolean> openProperty = room.getOpenProperty();
                                if(!openProperty.getValue(zombiesArena)) {
                                    openProperty.setValue(zombiesArena, true);
                                    newlyOpened.add(room.getRoomDisplayName());
                                }
                            }

                            if(newlyOpened.size() > 0) {
                                StringBuilder msg = new StringBuilder("opened ");
                                int i = 0;
                                for(String opened : newlyOpened) {
                                    msg.append(opened);

                                    if(i < newlyOpened.size() - 1) {
                                        msg.append(", ");
                                    }
                                }

                                for(ZombiesPlayer otherPlayer : zombiesArena.getPlayerMap().values()) {
                                    otherPlayer.getPlayer().showTitle(Title.title(Component.text(player.getName())
                                            .color(TextColor.color(255, 255, 0)), Component.text(msg.toString())
                                            .color(TextColor.color(61, 61, 61)), Title.Times.of(Duration.ofSeconds(1),
                                            Duration.ofSeconds(3), Duration.ofSeconds(1))));
                                }
                            }

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
    public ShopType getShopType() {
        return ShopType.DOOR;
    }
}
