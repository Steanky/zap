package io.github.zap.zombies.game;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.Metadata;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.event.EntityArgs;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.pathfind.ChunkBounds;
import io.github.zap.arenaapi.shadow.com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.ChunkLoadHandler;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.arena.damage.DamageHandler;
import io.github.zap.zombies.game.arena.event.ZombiesEventManager;
import io.github.zap.zombies.game.arena.round.RoundHandler;
import io.github.zap.zombies.game.arena.spawner.Spawner;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.equipment.EquipmentCreator;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentDataManager;
import io.github.zap.zombies.game.data.map.*;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.ShopCreator;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.data.map.shop.ShopDataManager;
import io.github.zap.zombies.game.data.map.shop.ShopMapping;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.melee.MeleeWeapon;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpBossBar;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import io.github.zap.zombies.game.powerups.managers.PowerUpDataManager;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.scoreboards.GameScoreboard;
import io.github.zap.zombies.game.shop.*;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.map.MapStats;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends Arena<ZombiesArena> implements Listener {

    @Getter
    private final MapData map;

    private final Hologram bestTimesHologram;

    @Getter
    private final EquipmentDataManager equipmentDataManager;

    private final @NotNull EquipmentCreator equipmentCreator;

    @Getter
    private final PowerUpDataManager powerUpDataManager;

    @Getter
    private final ShopDataManager shopDataManager;

    private final @NotNull ShopCreator shopCreator;

    @Getter
    private final StatsManager statsManager;

    @Getter
    private final DamageHandler damageHandler;

    private final RoundHandler roundHandler;

    @Getter
    private final Spawner spawner;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final Random random = new Random(); // TODO: static?

    @Getter
    private final long emptyTimeout;

    @Getter
    private final List<Shop<@NotNull ?>> shops = new ArrayList<>();

    @Getter
    private final @NotNull Map<String, List<Shop<@NotNull ?>>> shopMap = new HashMap<>();

    @Getter
    private final @NotNull Map<@NotNull String, @NotNull Event<@NotNull ShopEventArgs>> shopEvents = new HashMap<>();

    @Getter
    private String luckyChestRoom;

    @Getter
    private String piglinRoom;

    @Getter
    private final String corpseTeamName = UUID.randomUUID().toString().substring(0, 16);

    @Getter
    private final Set<Corpse> corpses = new HashSet<>();

    @Getter
    private final Set<Corpse> availableCorpses = new HashSet<>();

    @Getter
    private final ChunkLoadHandler chunkLoadHandler = new ChunkLoadHandler();

    @Getter
    private final GameScoreboard gameScoreboard;

    @Getter
    private final ChunkBounds mapBounds;

    @Getter
    private final Set<Player> hiddenPlayers = new HashSet<>() {
        @Override
        public boolean add(Player player) {
            if (super.add(player)) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.hidePlayer(Zombies.getInstance(), player);
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Player player && super.remove(o)) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.showPlayer(Zombies.getInstance(), player);
                }

                return true;
            }

            return false;
        }

    };

    /**
     * Indicate when the game start using System.currentTimeMillis()
     * return -1 if the game hasn't start
     */
    @Getter
    private long startTimeStamp = -1;

    /**
     * Indicate when the game end using System.currentTimeMillis()
     * return -1 if the game hasn't end
     */
    @Getter
    private long endTimeStamp = -1;

    @Getter
    private final Set<Pair<PowerUpSpawnRule<?>, String>> powerUpSpawnRules = new HashSet<>();

    // Contains both active and has not been picked up
    @Getter
    private final Set<PowerUp> powerUps = new HashSet<>();

    @Getter
    private final Event<PowerUpChangedEventArgs> powerUpChangedEvent = new Event<>();

    @Getter
    private final PowerUpBossBar powerUpBossBar = new PowerUpBossBar(this, 5);

    @Getter
    private int zombiesLeft;

    private RoundContext currentRound = null;

    private final Plugin plugin;

    private final ResourceManager resourceManager;

    private final PlayerList<ZombiesPlayer> playerList;

    private final BukkitTaskManager taskManager;

    private final ZombiesEventManager eventManager;

    private final Set<@NotNull Item> protectedItems;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map The map to use
     * @param world The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(@NotNull Plugin plugin, @NotNull ZombiesArenaManager manager, @NotNull World world,
                        @NotNull MapData map, @NotNull PlayerList<ZombiesPlayer> players,
                        @NotNull BukkitTaskManager taskManager, @NotNull ZombiesEventManager eventManager,
                        @NotNull ShopCreator shopCreator, @NotNull EquipmentCreator equipmentCreator,
                        @NotNull RoundHandler roundHandler, @NotNull Spawner spawner,
                        @NotNull DamageHandler damageHandler, @NotNull Set<@NotNull Item> protectedItems,
                        long emptyTimeout) {
        super(manager, world);
        this.resourceManager = new ResourceManager(plugin);
        this.plugin = plugin;
        this.map = map;
        this.equipmentDataManager = manager.getEquipmentDataManager();
        this.equipmentCreator = equipmentCreator;
        this.powerUpDataManager = manager.getPowerUpDataManager();
        this.shopDataManager = manager.getShopDataManager();
        this.shopCreator = shopCreator;
        this.statsManager = manager.getStatsManager();
        this.emptyTimeout = emptyTimeout;
        this.roundHandler = roundHandler;
        this.spawner = spawner;
        this.damageHandler = damageHandler;
        this.gameScoreboard = new GameScoreboard(this);
        gameScoreboard.initialize();

        this.playerList = players;
        this.taskManager = taskManager;
        this.eventManager = eventManager;
        this.protectedItems = protectedItems;
        registerArenaEvents();
        registerDisposables();

        bestTimesHologram = setupTimeLeaderboard();

        getMap().getPowerUpSpawnRules().forEach(x -> powerUpSpawnRules.add(Pair.of(getPowerUpDataManager()
                .createSpawnRule(x.getLeft(), x.getRight(), this), x.getRight())));

        BoundingBox bounds = map.getMapBounds();
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();

        mapBounds = new ChunkBounds(min.getBlockX() >> 4, min.getBlockZ() >> 4,
                (max.getBlockX() >> 4) + 1, (max.getBlockZ() >> 4) + 1);
    }

    private void registerArenaEvents() {
        eventManager.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        eventManager.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
        eventManager.getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        eventManager.getZombiesPlayerProxy(PlayerMoveEvent.class).registerHandler(this::onZombiesPlayerMove);
        eventManager.getZombiesPlayerProxy(PlayerInteractEvent.class).registerHandler(this::onZombiesPlayerInteract);
        eventManager.getZombiesPlayerProxy(PlayerInteractAtEntityEvent.class).registerHandler(this::onZombiesPlayerInteractAtEntity);
        eventManager.getZombiesPlayerProxy(PlayerItemHeldEvent.class).registerHandler(this::onZombiesPlayerItemHeld);
        eventManager.getZombiesPlayerDeathProxy(PlayerDeathEvent.class).registerHandler(this::onZombiesPlayerDeath);
        eventManager.getZombiesPlayerEntityProxy(EntityDamageEvent.class).registerHandler(this::onZombiesPlayerDamaged);
        eventManager.getMobProxy(EntityDamageEvent.class).registerHandler(this::onMobDamage);
        eventManager.getMobProxy(EntityDeathEvent.class).registerHandler(this::onMobDeath);
        eventManager.getMobProxy(EntityRemoveFromWorldEvent.class).registerHandler(this::onMobRemoveFromWorld);
        eventManager.getPlayerProxy(PlayerInteractEntityEvent.class).registerHandler(this::onPlayerInteractEntity);
        eventManager.getPlayerProxy(PlayerArmorStandManipulateEvent.class).registerHandler(this::onPlayerArmorStandManipulate);
        eventManager.getPlayerProxy(PlayerAttemptPickupItemEvent.class).registerHandler(this::onPlayerAttemptPickupItem);
        eventManager.getPlayerProxy(PlayerDropItemEvent.class).registerHandler(this::onPlayerDropItem);
        eventManager.getPlayerProxy(PlayerSwapHandItemsEvent.class).registerHandler(this::onPlayerSwapHandItems);
        eventManager.getPlayerProxy(PlayerItemDamageEvent.class).registerHandler(this::onPlayerItemDamage);
        eventManager.getPlayerProxy(PlayerItemConsumeEvent.class).registerHandler(this::onPlayerItemConsume);
        eventManager.getEntityProxy(EntityAddToWorldEvent.class).registerHandler(this::onEntityAddToWorld);
        eventManager.getEntityProxy(EntityDamageEvent.class).registerHandler(this::onEntityDamage);
        eventManager.getEntityProxy(EntityDamageByEntityEvent.class).registerHandler(this::onEntityDamageByEntity);
        eventManager.getEntityProxy(ItemDespawnEvent.class).registerHandler(this::onItemDespawn);
        eventManager.getEntityProxy(FoodLevelChangeEvent.class).registerHandler(this::onFoodLevelChange);
        eventManager.getBlockProxy(BlockPlaceEvent.class).registerHandler(this::onBlockPlace);
        eventManager.getBlockProxy(BlockBreakEvent.class).registerHandler(this::onBlockBreak);
        eventManager.getInventoryProxy(InventoryClickEvent.class).registerHandler(this::onPlayerInventoryClick);
    }

    private void registerDisposables() {
        resourceManager.addDisposable(taskManager);
        resourceManager.addDisposable(gameScoreboard);
        resourceManager.addDisposable(powerUpBossBar);
        resourceManager.addDisposable(chunkLoadHandler);
    }

    private @NotNull Hologram setupTimeLeaderboard() {
        Vector hologramLocation = map.getBestTimesLocation().clone()
                .add(new Vector(0, Hologram.DEFAULT_LINE_SPACE * map.getBestTimesCount(), 0));
        Hologram hologram = new Hologram(hologramLocation.toLocation(getWorld()));

        statsManager.queueCacheModification(CacheInformation.MAP, map.getName(), (stats) -> {
            ObjectMapper objectMapper = new ObjectMapper();

            List<Map.Entry<UUID, Integer>> bestTimes = new ArrayList<>(stats.getBestTimes().entrySet());
            bestTimes.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            int bound = Math.min(map.getBestTimesCount(), bestTimes.size());
            for (int i = 0; i < bound; i++) {
                Map.Entry<UUID, Integer> time = bestTimes.get(i);
                int finalI = i;

                Bukkit.getScheduler().runTask(Zombies.getInstance(), () -> {
                    if (startTimeStamp != -1) {
                        hologram.addLine(String.format("%s#%d %s- %s%s %s- %s%s", ChatColor.YELLOW, finalI,
                                ChatColor.WHITE, ChatColor.GRAY, "Loading...", ChatColor.WHITE, ChatColor.YELLOW,
                                TimeUtil.convertTicksToSecondsString(time.getValue())));
                    }
                });
            }
            for (int i = 0; i < bound; i++) {
                Map.Entry<UUID, Integer> time = bestTimes.get(i);
                int finalI = i;

                try {
                    String message =
                            IOUtils.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/"
                            + time.getKey().toString()), Charset.defaultCharset());

                    String name = objectMapper.readTree(message).get("name").textValue();
                    Bukkit.getScheduler().runTask(Zombies.getInstance(), () -> {
                        if (startTimeStamp != -1) {
                            hologram.updateLine(finalI, String.format("%s#%d %s- %s%s %s- %s%s", ChatColor.YELLOW,
                                    finalI, ChatColor.WHITE, ChatColor.GRAY, name, ChatColor.WHITE, ChatColor.YELLOW,
                                    TimeUtil.convertTicksToSecondsString(time.getValue())));
                        }
                    });
                } catch (IOException e) {
                    Zombies.warning("Failed to get name of player with UUID " + time.getKey().toString());
                }
            }
        }, MapStats::new);

        return hologram;
    }

    @Override
    public void dispose() {
        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
        manager.unloadArena(this);
    }


    @Override
    public boolean handleJoin(@NotNull Joinable joinable) {
        for (Pair<@NotNull List<@NotNull Player>, @NotNull Metadata> group : joinable.groups()) {
            Optional<?> playerTypeOptional = group.getRight().getMetadata("player_type");
            if (playerTypeOptional.isEmpty()) {
                return false;
            }

            List<@NotNull Player> newPlayers = new ArrayList<>();
            List<@NotNull ZombiesPlayer> rejoiningPlayers = new ArrayList<>();

            if (playerTypeOptional.get() instanceof String playerType) {
                switch (playerType) {
                    case "zombies_player" -> {
                        for (Player player : group.getLeft()) {
                            ZombiesPlayer zombiesPlayer = playerList.getPlayer(player.getUniqueId());
                            if (zombiesPlayer != null) {
                                if (!zombiesPlayer.isInGame()) {
                                    rejoiningPlayers.add(zombiesPlayer);
                                }
                            } else {
                                newPlayers.add(player);
                            }
                        }
                    }
                    // TODO: spectators
                    default -> {
                        return false;
                    }
                }
            }

            // perform checks on new/rejoining players
            if (!newPlayers.isEmpty() && !allowPlayerJoin(newPlayers)) {
                return false;
            }
            if (!rejoiningPlayers.isEmpty() && !allowPlayerRejoin(rejoiningPlayers)) {
                return false;
            }

            if (!newPlayers.isEmpty()) { // wrap players, call event
                for (Player player : newPlayers) {
                    playerList.addPlayer(player);
                }

                eventManager.getPlayerJoinEvent().callEvent(Collections.unmodifiableList(newPlayers));
            }

            if (rejoiningPlayers.size() > 0) { //rejoin players, call event
                for (ZombiesPlayer rejoiningPlayer : rejoiningPlayers) {
                    rejoiningPlayer.rejoin();
                }

                eventManager.getPlayerRejoinEvent().callEvent(Collections.unmodifiableList(rejoiningPlayers));
            }
        }

        return true;
    }

    @Override
    public void handleLeave(@NotNull List<@NotNull Player> leaving) {
        List<@NotNull ZombiesPlayer> leavers = new ArrayList<>();
        for (Player player : leaving) {
            ZombiesPlayer managedPlayer = playerList.getPlayer(player);

            if (managedPlayer != null && managedPlayer.isInGame()) {
                leavers.add(managedPlayer);
            }
        }

        if (!leavers.isEmpty()) {
            eventManager.getPlayerLeaveEvent().callEvent(Collections.unmodifiableList(leavers));
        }

        if (playerList.getOnlinePlayers().size() == 0) {
            // startTimeout(timeoutTicks);
        }

        // TODO: playerlist handling
        for(S player : leftPlayers) {
            player.quit();
        }
    }

    @Override
    public boolean hasPlayer(@NotNull UUID id) {
        return playerList.hasPlayer(id);
    }

    public boolean allowPlayers() {
        return state != ZombiesArenaState.ENDED && (state != ZombiesArenaState.STARTED || map.isAllowRejoin());
    }

    public boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                playerList.getOnlinePlayers().size() + players.size() <= map.getMaximumCapacity();
    }

    public boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
    }

    private void onPlayerJoin(@NotNull List<@NotNull Player> players) {
        if(state == ZombiesArenaState.PREGAME && playerList.getOnlinePlayers().size() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
        }

        if (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            for (Player player : players) {
                player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));

                player.showTitle(Title.title(Component.text("ZOMBIES", NamedTextColor.YELLOW),
                        map.getSplashScreenSubtitles().isEmpty()
                                ? Component.empty()
                                : Component.text(map.getSplashScreenSubtitles()
                                .get(random.nextInt(map.getSplashScreenSubtitles().size())))));
            }
            taskManager.runTask(() -> {
                if (startTimeStamp != -1) {
                    for (Player player : players) {
                        bestTimesHologram.renderToPlayer(player);
                    }
                }
            });
        }


        if (state == ZombiesArenaState.STARTED || state == ZombiesArenaState.ENDED) {
            for (Player player : players) {
                for (Player hiddenPlayer : hiddenPlayers) {
                    player.hidePlayer(Zombies.getInstance(), hiddenPlayer);
                }
                if (hiddenPlayers.contains(player)) {
                    for (Player otherPlayer : world.getPlayers()) {
                        otherPlayer.hidePlayer(Zombies.getInstance(), player);
                    }
                }
            }
        }
    }

    private void onPlayerRejoin(@NotNull List<@NotNull ZombiesPlayer> players) {
        for (ZombiesPlayer player : players) {
            player.getPlayer().teleport(WorldUtils.locationFrom(world, map.getSpawn()));

            for (Player hiddenPlayer : hiddenPlayers) {
                player.getPlayer().hidePlayer(Zombies.getInstance(), hiddenPlayer);
            }
            if (hiddenPlayers.contains(player.getPlayer())) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.hidePlayer(Zombies.getInstance(), player.getPlayer());
                }
            }
        }
    }

    private void onPlayerLeave(@NotNull List<@NotNull ZombiesPlayer> players) {
        for (ZombiesPlayer player : players) { //quit has already been called for these players
            if (!map.isAllowRejoin()) {
                playerList.removePlayer(player);
            }

            for (Player hiddenPlayer : hiddenPlayers) {
                player.getPlayer().showPlayer(plugin, hiddenPlayer);
            }
            if (hiddenPlayers.contains(player.getPlayer())) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.showPlayer(plugin, player.getPlayer());
                }
            }
        }

        stateLabel:
        switch (state) {
            case PREGAME:
                for (ZombiesPlayer player : players) {
                    playerList.removePlayer(player);
                }
                break;
            case COUNTDOWN:
                for (ZombiesPlayer player : players) {
                    playerList.removePlayer(player);
                }

                if (playerList.getOnlinePlayers().size() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                }
                break;
            case STARTED:
                if (playerList.getOnlinePlayers().isEmpty()) {
                    state = ZombiesArenaState.ENDED;
                    dispose(); //shut everything down immediately if everyone leaves mid-game
                } else {
                    for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
                        if (!players.contains(player) && player.isAlive()) {
                            break stateLabel;
                        }
                    }

                    // There are no players alive, so end the game
                    for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
                        player.kill();
                    }
                    doLoss();
                }
                break;
        }
    }

    private void onZombiesPlayerMove(@NotNull ManagedPlayerArgs<ZombiesPlayer, PlayerMoveEvent> args) {
        // disgusting! but works (allows head rotation but not movement)
        if (args.player().getState() == ZombiesPlayerState.KNOCKED
                && !args.event().getFrom().toVector().equals(args.event().getTo().toVector())) {
            args.event().setCancelled(true);
        }
    }

    private void onZombiesPlayerInteract(@NotNull ManagedPlayerArgs<ZombiesPlayer, PlayerInteractEvent> args) {
        PlayerInteractEvent event = args.event();
        ZombiesPlayer player = args.player();
        Action action = event.getAction();

        if (event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noInteractions = true;
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                for (Shop<@NotNull ?> shop : shops) {
                    if (shop.interact(args)) {
                        noInteractions = false;
                        break;
                    }
                }
            }
            if (noInteractions) {
                player.getHotbarManager().click(event.getAction());
            }
        }

        event.setCancelled(true);
    }

    private void onZombiesPlayerInteractAtEntity(ManagedPlayerArgs<ZombiesPlayer, PlayerInteractAtEntityEvent> args) {
        PlayerInteractAtEntityEvent event = args.event();
        ZombiesPlayer player = args.player();

        if (args.event().getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noInteractions = true;
            for (Shop<@NotNull ?> shop : shops) {
                if (shop.interact(args)) {
                    noInteractions = false;
                    break;
                }
            }
            if (noInteractions) {
                player.getHotbarManager().click(Action.RIGHT_CLICK_BLOCK);
            }
        }

        event.setCancelled(true);
    }

    private void onZombiesPlayerItemHeld(@NotNull ManagedPlayerArgs<ZombiesPlayer, PlayerItemHeldEvent> args) {
        ZombiesPlayer managedPlayer = args.player();
        managedPlayer.getHotbarManager().setSelectedSlot(args.event().getNewSlot());
    }

    private void onZombiesPlayerDeath(ManagedPlayerArgs<ZombiesPlayer, PlayerDeathEvent> args) {
        args.event().setCancelled(true); // cancel death event

        if (state == ZombiesArenaState.STARTED) {
            ZombiesPlayer knocked = args.player();

            if (knocked.getState() == ZombiesPlayerState.ALIVE) {
                knocked.knock();

                Collection<ZombiesPlayer> players = playerList.getOnlinePlayers();
                for (ZombiesPlayer player : players) {
                    if (player.isAlive()) {
                        String message = knocked.getDeathRoomName();

                        // display death message only if necessary
                        for (ZombiesPlayer otherPlayer : players) {
                            Player otherBukkitPlayer = otherPlayer.getPlayer();

                            if (otherPlayer != knocked) {
                                otherBukkitPlayer.showTitle(Title.title(Component.text(knocked.getPlayer().getName())
                                        .color(TextColor.color(255, 255, 0)), Component.text("was knocked down in " + message)
                                        .color(TextColor.color(61, 61, 61)), Title.Times.of(Duration.ofSeconds(1),
                                        Duration.ofSeconds(3), Duration.ofSeconds(1))));


                                otherPlayer.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.ender_dragon.growl"), Sound.Source.MASTER, 1.0F, 0.5F));
                            }
                        }

                        return; // return if there are any players still alive
                    }
                }

                // There are no players alive, so end the game
                for (ZombiesPlayer player : players) {
                    player.kill();
                }
                doLoss();
            }
        }
    }

    private void onZombiesPlayerDamaged(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull EntityDamageEvent> args) {
        Player player = args.player().getPlayer();

        if (args.player().isAlive()) {
            if (player.getHealth() <= args.event().getFinalDamage()) {
                Location location = player.getLocation();
                location.setY(Math.floor(location.getY()));

                RoomData room = map.roomAt(location.toVector());
                args.player().setDeathRoomName((room == null) ? "an unknown room" : room.getRoomDisplayName());

                for (double y = location.getY() - 1.0D; y >= 0D; y--) {
                    location.setY(y);
                    Block block = player.getWorld().getBlockAt(location);

                    if (!block.isPassable()) {
                        player.teleport(location.add(0, block.getBoundingBox().getHeight(), 0));
                        break;
                    }
                }
            }
        } else {
            args.event().setCancelled(true);
        }
    }

    private void onMobDamage(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityDamageEvent> args) {
        if (args.event().getCause() == EntityDamageEvent.DamageCause.VOID
                && spawner.getMobs().contains(args.entity())) {
            args.entity().remove();
        }
    }

    private void onMobDeath(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityDeathEvent> args) {
        if (state == ZombiesArenaState.STARTED) {
            spawner.onMobDeath(args);
        }
    }

    private void onMobRemoveFromWorld(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityRemoveFromWorldEvent> args) {
        if (state == ZombiesArenaState.STARTED) {
            spawner.onMobRemoveFromWorld(args);
        }
    }

    private void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity.getWorld().equals(world) && entity instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    private void onPlayerArmorStandManipulate(@NotNull PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    private void onPlayerAttemptPickupItem(@NotNull PlayerAttemptPickupItemEvent event) {
        event.setCancelled(true);
    }

    private void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        ItemStack playerStack = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

        if (playerStack == null) {
            event.setCancelled(true);
        } else {
            Item dropped = event.getItemDrop();
            dropped.remove();

            if (playerStack.getType() != Material.AIR) {
                playerStack.setAmount(playerStack.getAmount() + 1);
            } else {
                playerStack.setType(dropped.getItemStack().getType());
                event.setCancelled(true);
            }

            event.getPlayer().updateInventory();
        }
    }

    private void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    private void onPlayerItemDamage(@NotNull PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    private void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        event.setCancelled(true);
    }

    private void onPlayerInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player bukkitPlayer
                && bukkitPlayer.getInventory().equals(event.getClickedInventory())) {
            ZombiesPlayer player = playerList.getPlayer(bukkitPlayer);
            if (player != null && player.isInGame()) {
                event.setCancelled(true);
            }
        }
    }

    private void onEntityAddToWorld(@NotNull EntityAddToWorldEvent event) {
        // only necessary so long as MythicMobs refuses to work right
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.CHICKEN && (!(entity instanceof Mob mob)
                || !spawner.getMobs().contains(mob))) {
            for (Entity passenger : entity.getPassengers()) {
                entity.removePassenger(passenger);
            }

            entity.remove();
        }
    }

    private void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting) {
            event.setCancelled(true);
        }
    }

    private void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        ZombiesPlayer damagingPlayer = playerList.getPlayer(event.getDamager().getUniqueId());

        if (damagingPlayer != null) {
            if (!damagingPlayer.isAlive()) {
                event.setCancelled(true);
            } else if (entity instanceof Mob mob && spawner.getMobs().contains(mob)) {
                HotbarManager hotbarManager = damagingPlayer.getHotbarManager();
                HotbarObject hotbarObject = hotbarManager.getSelectedObject();

                if (hotbarObject instanceof MeleeWeapon<@NotNull ?, @NotNull ?> meleeWeapon) {
                    if (meleeWeapon.isUsable() && (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                                    || meleeWeapon.getCurrentLevel().isShouldSweep())) {
                        event.setDamage(0D);
                        meleeWeapon.attack(mob);
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void onItemDespawn(@NotNull ItemDespawnEvent event) {
        if (protectedItems.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private void onFoodLevelChange(@NotNull FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    private void onBlockPlace(@NotNull BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    private void onBlockBreak(@NotNull BlockBreakEvent event) {
        event.setCancelled(true);
    }

    public void startGame() {
        if (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            loadShops();

            state = ZombiesArenaState.STARTED;
            startTimeStamp = System.currentTimeMillis();

            taskManager.runTask(bestTimesHologram::destroy);

            for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
                player.getPlayer().sendMessage(Component.text("Started!", NamedTextColor.YELLOW));
                player.setAliveState();

                Vector spawn = map.getSpawn();
                player.getPlayer().teleport(new Location(world, spawn.getX() + 0.5, spawn.getY(),
                        spawn.getZ() + 0.5));

                ZombiesHotbarManager hotbarManager = player.getHotbarManager();
                for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : map
                        .getHotbarObjectGroupSlots().entrySet()) {
                    hotbarManager.addEquipmentObjectGroup(equipmentCreator
                            .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), player.getPlayer(),
                                    hotbarObjectGroupSlot.getValue()));
                }

                for(String deafultEquipment : map.getDefaultEquipments()) {
                    EquipmentData<?> equipmentData = equipmentDataManager.getEquipmentData(map.getName(), deafultEquipment);

                    if(equipmentData != null) {
                        HotbarObjectGroup hotbarObjectGroup = hotbarManager
                                .getHotbarObjectGroup(equipmentData.getEquipmentObjectGroupType());

                        if (hotbarObjectGroup != null) {
                            Integer slot = hotbarObjectGroup.getNextEmptySlot();

                            if (slot != null) {
                                Equipment<@NotNull ?, @NotNull ?> equipment = equipmentCreator.createEquipment(player, slot, equipmentData);
                                if (equipment != null) {
                                    hotbarManager.setHotbarObject(slot, equipment);
                                }
                                else {
                                    Zombies.warning("Failed to create default equipment " + deafultEquipment + "!");
                                }
                            }
                        }
                    }
                    else {
                        Zombies.warning("Default equipment " + deafultEquipment + " does not exist!");
                    }
                }

                statsManager.queueCacheModification(CacheInformation.PLAYER, player.getId(),
                        (stats) -> {
                    PlayerMapStats mapStats = stats.getMapStatsForMap(map);
                    mapStats.setTimesPlayed(mapStats.getTimesPlayed() + 1);
                    }, PlayerGeneralStats::new);

                player.startTasks();
            }

            roundHandler.onGameBegin();
        }
    }

    /**
     * Win code here
     */
    private void doVictory() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        int duration = (int) ((endTimeStamp - startTimeStamp) / 1000);

        Integer round = map.getCurrentRoundProperty().getValue(this);
        for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
            player.getPlayer().showTitle(Title.title(Component.text("You Win!", NamedTextColor.GREEN),
                    Component.text("You made it to Round " + round + "!", NamedTextColor.GRAY)));
            statsManager.queueCacheModification(CacheInformation.PLAYER, player.getPlayer().getUniqueId(),
                    (playerStats) -> {
                PlayerMapStats playerMapStats = playerStats.getMapStatsForMap(map);
                playerMapStats.setWins(playerMapStats.getWins() + 1);

                if (playerMapStats.getBestTime() == null || duration < playerMapStats.getBestTime()) {
                    playerMapStats.setBestTime(duration);
                    statsManager.queueCacheModification(CacheInformation.MAP, map.getName(), (mapStats) -> {
                        Map<UUID, Integer> bestTimes = mapStats.getBestTimes();
                        bestTimes.put(player.getId(), duration);
                        }, MapStats::new);
                }
                }, PlayerGeneralStats::new);
        }
        taskManager.runTaskLater(200L, this::dispose);
    }

    /**
     * Loss code here
     */
    private void doLoss() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
            player.getPlayer().showTitle(Title.title(Component.text("Game over!", NamedTextColor.GREEN),
                    Component.text("You made it to Round " + round + "!", NamedTextColor.GRAY)));
        }
        gameScoreboard.run();
        taskManager.runTaskLater(200L, this::dispose);
    }

    /**
     * Attempts to break the given window.
     */
    public void tryBreakWindow(Entity attacker, WindowData targetWindow, int by) {
        targetWindow.getAttackingEntityProperty().setValue(this, attacker);

        int previousIndex = targetWindow.getCurrentIndexProperty().getValue(this);
        int blocksBroken = targetWindow.retractRepairState(this, by);

        for(int i = previousIndex; i > previousIndex - blocksBroken; i--) { //break the blocks
            WorldUtils.getBlockAt(world, targetWindow.getFaceVectors().get(i)).setType(Material.AIR);

            Vector center = targetWindow.getCenter();
            if(i > 0) {
                world.playSound(targetWindow.getBlockBreakSound(), center.getX(), center.getY(), center.getZ());
            }
            else {
                world.playSound(targetWindow.getWindowBreakSound(), center.getX(), center.getY(), center.getZ());
            }
        }
    }

    public boolean runAI() {
        return state == ZombiesArenaState.STARTED;
    }

    /**
     * Loads shops; should be called just before the game begins
     */
    private void loadShops() {
        for (ShopData shopData : map.getShops()) {
            if (shopData != null) {
                Shop<@NotNull ?> shop = shopCreator.createShop(shopData);
                if (shop != null) {
                    shops.add(shop);
                    shopMap.computeIfAbsent(shop.getShopType(), (unused) -> new ArrayList<>()).add(shop);
                    getShopEvent(shop.getShopType());
                    shop.display();
                }
            }
        }

        for(DoorData doorData : map.getDoors()) {
            if (doorData != null) {
                Shop<@NotNull DoorData> shop = shopCreator.createShop(doorData);
                if (shop != null) {
                    shops.add(shop);
                    shopMap.computeIfAbsent(shop.getShopType(), (unused) -> new ArrayList<>()).add(shop);
                    shop.display();
                }
            }
        }
        getShopEvent(ShopType.DOOR.name());

        for (Shop<?> shop : shopMap.get(ShopType.TEAM_MACHINE.name())) {
            TeamMachine teamMachine = (TeamMachine) shop;
            resourceManager.addDisposable(teamMachine);
        }

        Event<ShopEventArgs> chestEvent = getShopEvent(ShopType.LUCKY_CHEST.name());
        List<Shop<?>> shopMapChests = shopMap.get(ShopType.LUCKY_CHEST.name());
        if (chestEvent != null && shopMapChests != null) {
            chestEvent.registerHandler(new EventHandler<>() {

                private final Random random = new Random();
                int rolls = 0;
                {
                    List<Shop<?>> chests = new ArrayList<>(shopMapChests);

                    if (!map.isChestCanStartInSpawnRoom()) {
                        RoomData spawnRoom = map.roomAt(map.getSpawn());
                        chests.removeIf(chest -> {
                            LuckyChest luckyChest = (LuckyChest) chest;
                            return spawnRoom.getBounds().contains(luckyChest.getShopData().getChestLocation());
                        });
                    }

                    LuckyChest luckyChest
                            = (LuckyChest) chests.get(random.nextInt(chests.size()));
                    luckyChest.setActive(true);

                    RoomData room = map.roomAt(luckyChest.getShopData().getChestLocation());
                    luckyChestRoom = room != null ? room.getRoomDisplayName() : null;
                }

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        LuckyChest luckyChest = (LuckyChest) args.shop();
                        luckyChest.setActive(false);
                        List<Shop<?>> chests = new ArrayList<>(shopMap.get(luckyChest.getShopType()));
                        chests.remove(luckyChest);

                        LuckyChest nextLuckyChest = ((LuckyChest) chests.get(random.nextInt(chests.size())));
                        nextLuckyChest.setActive(true);
                        RoomData room = map.roomAt(nextLuckyChest.getShopData().getChestLocation());
                        luckyChestRoom = room != null ? room.getRoomDisplayName() : null;

                        rolls = 0;
                    }
                }
            });
        }
        Event<ShopEventArgs> piglinShopEvent = getShopEvent(ShopType.PIGLIN_SHOP.name());
        List<Shop<?>> shopMapPiglins = shopMap.get(ShopType.PIGLIN_SHOP.name());
        if (piglinShopEvent != null && shopMapPiglins != null) {
            piglinShopEvent.registerHandler(new EventHandler<>() {

                private final Random random = new Random();
                int rolls = 0;
                {
                    List<Shop<?>> piglins = new ArrayList<>(shopMapPiglins);

                    if (!map.isChestCanStartInSpawnRoom()) {
                        RoomData spawnRoom = map.roomAt(map.getSpawn());
                        piglins.removeIf(piglin -> {
                            PiglinShop piglinShop = (PiglinShop) piglin;
                            return spawnRoom.getBounds().contains(piglinShop.getShopData().getPiglinLocation());
                        });
                    }

                    PiglinShop piglinShop
                            = (PiglinShop) piglins.get(random.nextInt(piglins.size()));
                    piglinShop.setActive(true);

                    RoomData room = map.roomAt(piglinShop.getShopData().getPiglinLocation());
                    piglinRoom = room != null ? room.getRoomDisplayName() : null;
                }

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        PiglinShop piglinShop = (PiglinShop) args.shop();
                        piglinShop.setActive(false);
                        List<Shop<?>> piglins = new ArrayList<>(shopMap.get(piglinShop.getShopType()));
                        piglins.remove(piglinShop);

                        PiglinShop nextPiglinShop = ((PiglinShop) piglins.get(random.nextInt(piglins.size())));
                        nextPiglinShop.setActive(true);
                        RoomData room = map.roomAt(nextPiglinShop.getShopData().getPiglinLocation());
                        piglinRoom = room != null ? room.getRoomDisplayName() : null;

                        rolls = 0;
                    }
                }
            });
        }
    }
}
