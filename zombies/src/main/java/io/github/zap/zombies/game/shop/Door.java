package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.DoorSide;
import io.github.zap.zombies.game.perk.PerkType;
import io.github.zap.zombies.game.perk.SpeedPerk;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
                MapData map = getZombiesArena().getMap();
                if (opensTo.size() > 0) {
                    stringBuilder.append(map.getNamedRoom(opensTo.get(0)).getRoomDisplayName());
                    for (int i = 1; i < opensTo.size(); i++) {
                        stringBuilder.append(" & ");
                        stringBuilder.append(map.getNamedRoom(opensTo.get(i)).getRoomDisplayName());
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

            if (zombiesPlayer != null) {
                Player player = zombiesPlayer.getPlayer();

                PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) args.getEvent();
                Block block = playerInteractEvent.getClickedBlock();

                if (player != null && block != null && !block.getType().isAir()
                        && doorData.getDoorBounds().contains(block.getLocation().toVector())) {

                    boolean anySide = false;
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
                                for (String openedRoom : doorSide.getOpensTo()) {
                                    RoomData room = zombiesArena.getMap().getNamedRoom(openedRoom);
                                    Property<Boolean> openProperty = room.getOpenProperty();
                                    if (!openProperty.getValue(zombiesArena)) {
                                        openProperty.setValue(zombiesArena, true);
                                        newlyOpened.add(room.getRoomDisplayName());
                                    }
                                }

                                if (newlyOpened.size() > 0) {
                                    StringBuilder msg = new StringBuilder("opened ");
                                    int i = 0;
                                    for (String opened : newlyOpened) {
                                        msg.append(opened);

                                        if (i < newlyOpened.size() - 1) {
                                            msg.append(", ");
                                        }
                                    }

                                    for (ZombiesPlayer otherPlayer : zombiesArena.getPlayerMap().values()) {
                                        Player otherBukkitPlayer = otherPlayer.getPlayer();
                                        if (otherBukkitPlayer != null) {
                                            otherBukkitPlayer.showTitle(Title.title(Component.text(player.getName())
                                                    .color(TextColor.color(255, 255, 0)), Component.text(msg.toString())
                                                    .color(TextColor.color(61, 61, 61)), Title.Times.of(Duration.ofSeconds(1),
                                                    Duration.ofSeconds(3), Duration.ofSeconds(1))));
                                        }
                                    }
                                }

                                for (Hologram hologram : doorSideHologramMap.values()) {
                                    hologram.destroy();
                                }

                                MapData map = zombiesArena.getMap();
                                PotionEffect speedEffect = new PotionEffect(
                                        PotionEffectType.SPEED,
                                        map.getDoorSpeedTicks(),
                                        map.getDoorSpeedLevel(),
                                        true,
                                        false,
                                        false
                                );
                                player.addPotionEffect(speedEffect);

                                SpeedPerk speedPerk = (SpeedPerk) zombiesPlayer.getPerks().getPerk(PerkType.SPEED);
                                zombiesArena.runTaskLater(map.getDoorSpeedTicks(),
                                        () ->  speedPerk.execute(EmptyEventArgs.getInstance()));

                                zombiesArena.getStatsManager().modifyStatsForPlayer(player, (stats) -> {
                                    PlayerMapStats mapStats = stats.getMapStatsForMap(zombiesArena.getMap());
                                    mapStats.setDoorsOpened(mapStats.getDoorsOpened() + 1);
                                });

                                opened = true;
                                onPurchaseSuccess(zombiesPlayer);

                                return true;
                            }

                            anySide = true;
                            break;
                        }
                    }

                    if (!anySide) {
                        player.sendMessage(Component
                                .text("You can't open the door from here!")
                                .color(NamedTextColor.RED)
                        );
                    }

                    player.playSound(Sound.sound(
                            Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER,
                            1.0F,
                            0.5F
                    ));

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.DOOR;
    }
}
