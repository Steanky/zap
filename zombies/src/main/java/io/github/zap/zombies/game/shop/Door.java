package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.shop.DoorData;
import io.github.zap.zombies.game.data.shop.DoorSide;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.Speed;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
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
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a door used to open other rooms
 */
public class Door extends Shop<@NotNull DoorData> {

    private final @NotNull Map<DoorSide, Hologram> doorSideHologramMap = new HashMap<>();

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull MapData map;

    private final @NotNull StatsManager statsManager;

    private boolean opened = false;

    public Door(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull DoorData shopData,
                @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList, @NotNull MapData map,
                @NotNull StatsManager statsManager) {
        super(world, eventManager, shopData);

        this.playerList = playerList;
        this.map = map;
        this.statsManager = statsManager;

        for (DoorSide doorSide : getShopData().getDoorSides()) {
            Hologram hologram = new Hologram(doorSide.getHologramLocation().toLocation(world));
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine("");
            }

            doorSideHologramMap.put(doorSide, hologram);
        }
    }

    @Override
    public void onPlayerJoin(@NotNull List<@NotNull Player> players) {
        for (Hologram hologram : doorSideHologramMap.values()) {
            for (Player player : players) {
                hologram.renderToPlayer(player);
            }
        }

        super.onPlayerJoin(players);
    }

    @Override
    public void onPlayerRejoin(@NotNull List<? extends @NotNull ZombiesPlayer> players) {
        for (Hologram hologram : doorSideHologramMap.values()) {
            for (ZombiesPlayer player : players) {
                hologram.renderToPlayer(player.getPlayer());
            }
        }

        super.onPlayerRejoin(players);
    }

    @Override
    public void display() {
        if (!opened) {
            for (Map.Entry<DoorSide, Hologram> entry : doorSideHologramMap.entrySet()) {
                Hologram hologram = entry.getValue();

                hologram.updateLineForEveryone(0, getDoorDisplayName(entry));
                hologram.updateLineForEveryone(1,
                        ChatColor.GOLD.toString() + entry.getKey().getCost() + " Gold");
            }
        }
    }

    private String getDoorDisplayName(@NotNull Map.Entry<DoorSide, Hologram> entry) {
        StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN.toString());
        List<String> opensTo = entry.getKey().getOpensTo();
        if (opensTo.size() > 0) {
            stringBuilder.append(map.getNamedRoom(opensTo.get(0)).getRoomDisplayName());
            for (int i = 1; i < opensTo.size(); i++) {
                stringBuilder.append(" & ");
                stringBuilder.append(map.getNamedRoom(opensTo.get(i)).getRoomDisplayName());
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (args.event() instanceof PlayerInteractEvent event) {
            DoorData doorData = getShopData();
            ZombiesPlayer player = args.player();

            Block block = event.getClickedBlock();
            if (block != null && !block.getType().isAir()
                    && doorData.getDoorBounds().contains(block.getLocation().toVector())) {
                if (!attemptToOpenDoor(doorData, player)) {
                    player.getPlayer().sendMessage(Component.text("You can't open the door from here!",
                            NamedTextColor.RED));
                    player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER, 1.0F, 0.5F));
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

    /**
     * Attempts to open the door
     * @param doorData The doordata of the door
     * @param player The player that attempted to open the door
     * @return Whether an interaction was made
     */
    private boolean attemptToOpenDoor(@NotNull DoorData doorData, @NotNull ZombiesPlayer player) {
        Location playerLocation = player.getPlayer().getLocation();

        for (DoorSide doorSide : doorData.getDoorSides()) {
            if (doorSide.getTriggerBounds().contains(playerLocation.toVector())) {
                int cost = doorSide.getCost();

                if (player.getCoins() < cost) {
                    player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                            NamedTextColor.RED));
                } else {
                    WorldUtils.fillBounds(getWorld(), doorData.getDoorBounds(), map.getDoorFillMaterial());
                    getWorld().playSound(doorData.getOpenSound(), playerLocation.getX(),
                            playerLocation.getY(), playerLocation.getZ());

                    openOtherDoors(doorSide, player.getPlayer());
                    applySpeedToPlayer(player);
                    incrementDoorsOpenedStat(player.getPlayer());

                    player.subtractCoins(cost);

                    opened = true;
                    onPurchaseSuccess(player);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Marks connected doors as opened
     * @param doorSide The door side from which the door was opened
     * @param opener The player that opened the door
     */
    private void openOtherDoors(@NotNull DoorSide doorSide, @NotNull Player opener) {
        List<String> newlyOpened = new ArrayList<>();
        for (String openedRoom : doorSide.getOpensTo()) {
            RoomData room = map.getNamedRoom(openedRoom);
            Property<Boolean> openProperty = room.getOpenProperty();

            if (!openProperty.getValue(arena)) {
                openProperty.setValue(arena, true);
                newlyOpened.add(room.getRoomDisplayName());
            }
        }

        for (Hologram hologram : doorSideHologramMap.values()) {
            hologram.destroy();
        }

        sendOpenMessage(newlyOpened, opener);
    }

    /**
     * Sends the message to notify players that a door has been opened
     * @param newlyOpened A list of the door names that were opened
     * @param opener The player that opened the door
     */
    private void sendOpenMessage(@NotNull List<String> newlyOpened, @NotNull Player opener) {
        if (!newlyOpened.isEmpty()) {
            StringBuilder message = new StringBuilder("opened ");
            int i = 0;
            for (String opened : newlyOpened) {
                message.append(opened);

                if (i < newlyOpened.size() - 1) {
                    message.append(", ");
                }
            }

            for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
                player.getPlayer().showTitle(Title.title(Component.text(opener.getName(), NamedTextColor.YELLOW),
                        Component.text(message.toString(), TextColor.color(61, 61, 61)),
                        Title.Times.of(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))));
            }
        }
    }

    /**
     * Applies speed to a player after opening a door
     * @param opener The player that opened the door
     */
    private void applySpeedToPlayer(@NotNull ZombiesPlayer opener) {
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED,
                map.getDoorSpeedTicks(), map.getDoorSpeedLevel(), true, false,
                false);
        opener.getPlayer().addPotionEffect(speedEffect);

        HotbarManager hotbarManager = opener.getHotbarManager();
        HotbarObjectGroup hotbarObjectGroup = hotbarManager
                .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());

        if (hotbarObjectGroup != null) {
            for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                if (hotbarObject instanceof Speed speed) {
                    speed.activate(); // update speed to synchronize timings
                    break;
                }
            }
        }
    }

    /**
     * Increments the player's door open count
     * @param opener The player that opened the door
     */
    private void incrementDoorsOpenedStat(@NotNull Player opener) {
        statsManager.queueCacheModification(CacheInformation.PLAYER,
                opener.getUniqueId(), (stats) -> {
                    PlayerMapStats mapStats = stats.getMapStatsForMap(map);
                    mapStats.setDoorsOpened(mapStats.getDoorsOpened() + 1);
                }, PlayerGeneralStats::new);
    }

}
