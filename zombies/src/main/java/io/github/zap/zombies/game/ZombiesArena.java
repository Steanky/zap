package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.*;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> implements Listener {
    @Getter
    private final MapData map;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final long emptyTimeout;

    @Getter
    private final Spawner spawner = new RangelessSpawner();

    @Getter
    private final Set<UUID> mobs = new HashSet<>();

    private final List<Integer> waveSpawnerTasks = new ArrayList<>();
    private int timeoutTaskId = -1;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map          The map to use
     * @param world        The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, long emptyTimeout) {
        super(Zombies.getInstance(), manager, world, (arena, player) -> new ZombiesPlayer(arena, player, arena.getMap()
                .getStartingCoins()));

        this.map = map;
        this.emptyTimeout = emptyTimeout;

        Event<EntityDeathEvent> entityDeathEvent = new ProxyEvent<>(Zombies.getInstance(), this,
                EntityDeathEvent.class);
        entityDeathEvent.registerHandler(this::onMobDeath);

        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        getPlayerDeathEvent().registerHandler(this::onPlayerDeath);
        getPlayerInteractEvent().registerHandler(this::onPlayerInteract);
        getPlayerToggleSneakEvent().registerHandler(this::onPlayerSneak);
    }

    @Override
    protected ZombiesArena getArena() {
        return this;
    }

    @Override
    public void dispose() {
        super.dispose(); //dispose of superclass-specific resources

        //unregister tasks
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTask(timeoutTaskId);

        for(int taskId : waveSpawnerTasks) {
            scheduler.cancelTask(taskId);
        }

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
    }

    @Override
    protected boolean allowPlayers() {
        return state != ZombiesArenaState.ENDED && (state != ZombiesArenaState.STARTED || map.isAllowRejoin());
    }

    @Override
    protected boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                getOnlineCount() + players.size() <= map.getMaximumCapacity();
    }

    protected boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
    }

    private void onPlayerJoin(PlayerListArgs args) {
        if(state == ZombiesArenaState.PREGAME && getOnlineCount() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
            startCountdown();
        }

        for(Player player : args.getPlayers()) {
            player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));
        }

        resetTimeout(); //if arena was in timeout state, reset that
    }

    private void onPlayerLeave(ManagedPlayerListArgs args) {
        switch (state) {
            case PREGAME:
                if (getOnlineCount() == 0) {
                    startTimeout();
                }

                removePlayers(args.getPlayers());
                break;
            case COUNTDOWN:
                if (getOnlineCount() == 0) {
                    startTimeout();
                }
                if (getOnlineCount() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                    stopCountdown();
                }

                removePlayers(args.getPlayers());
                break;
            case STARTED:
                if (getOnlineCount() == 0) {
                    state = ZombiesArenaState.ENDED;
                    dispose(); //shut everything down immediately if everyone leaves mid-game
                }
                break;
        }

        if(!map.isAllowRejoin()) {
            for(ZombiesPlayer player : args.getPlayers()) {
                super.removePlayer(player);
            }
        }
    }

    private void onMobDeath(EntityDeathEvent args) {
        mobs.remove(args.getEntity().getUniqueId());
        if (mobs.size() == 0 && state == ZombiesArenaState.STARTED) { //round ended, begin next one
            doRound();
        }
    }

    private void onPlayerDeath(ProxyArgs<PlayerDeathEvent> args) {
        ZombiesPlayer managedPlayer = args.getManagedPlayer();
        managedPlayer.knock();

        for(ZombiesPlayer player : getPlayerMap().values()) {
            if(player.isAlive()) {
                return; //return if there are any players still alive
            }
        }

        doLoss();
    }

    private void onPlayerInteract(ProxyArgs<PlayerInteractEvent> args) {
        PlayerInteractEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if(event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            Block block = args.getEvent().getClickedBlock();

            if(block != null) {
                Vector clickedVector = block.getLocation().toVector();
                if(!player.tryOpenDoor(clickedVector)) {
                    //TODO: perform other actions involving right-clicking on a block
                }
            }
            else {
                //TODO: perform actions involving rightclick on air
            }
        }
    }

    private void onPlayerSneak(ProxyArgs<PlayerToggleSneakEvent> args) {
        PlayerToggleSneakEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(event.isSneaking()) {
            managedPlayer.activateRepair();
        }
        else {
            managedPlayer.disableRepair();
        }
    }

    public List<ActiveMob> spawnMobs(List<MythicMob> mobs, Spawner spawner) {
        List<ActiveMob> activeMobs = new ArrayList<>();

        while(true) {
            boolean spawned = false;

            for(RoomData room : map.getRooms()) { //iterate through all rooms
                if(room.isSpawn() || room.getOpenProperty().get(this)) { //only try to spawn in open rooms
                    for(SpawnpointData spawnpoint : room.getSpawnpoints()) {
                        for(int i = mobs.size() - 1; i >= 0; i--) {
                            MythicMob mythicMob = mobs.get(i);

                            if(spawner.canSpawn(this, spawnpoint, mythicMob)) {
                                ActiveMob activeMob = spawner.spawnAt(this, spawnpoint, mythicMob);
                                mobs.remove(i);

                                if(activeMob != null) {
                                    this.mobs.add(activeMob.getUniqueId());
                                    activeMobs.add(activeMob);
                                    spawned = true;
                                    break;
                                }

                                if(mobs.size() == 0) { //avoid redundant iteration when all mobs have been spawned
                                    return activeMobs;
                                }
                            }
                        }
                    }
                }
            }

            if(!spawned) {
                Zombies.warning("Some enemies could not be spawned.");
                return activeMobs;
            }
        }
    }

    private void doRound() {
        for(ZombiesPlayer player : getPlayerMap().values()) { //respawn players who may have died
            if(!player.isAlive()) {
                player.respawn();
            }
        }

        Property<Integer> currentRoundProperty = map.getCurrentRoundProperty();
        int currentRoundIndex = currentRoundProperty.get(this);

        List<RoundData> rounds = map.getRounds();
        if(currentRoundIndex < rounds.size()) {
            RoundData currentRound = rounds.get(currentRoundIndex);

            long cumulativeDelay = 0;
            for (WaveData wave : currentRound.getWaves()) {
                cumulativeDelay += wave.getWaveLength();
                List<MythicMob> mobs = new ArrayList<>();

                for(String mobName : wave.getMobs()) { //convert mob names to MythicMob instances
                    MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);
                    if(mob != null) {
                        mobs.add(mob);
                    }
                    else {
                        Zombies.warning(String.format("Tried to spawn non-existant MythicMob with name %s.", mobName));
                    }
                }

                waveSpawnerTasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> {
                    spawnMobs(mobs, spawner);
                    waveSpawnerTasks.remove(0);
                }, cumulativeDelay));
            }

            currentRoundProperty.set(this, currentRoundIndex + 1);
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

    private void startTimeout() {
        if(timeoutTaskId == -1) {
            timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), this::dispose,
                    emptyTimeout);
        }
    }

    private void resetTimeout() {
        if(timeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
            timeoutTaskId = -1;
        }
    }

    private void startCountdown() {
        //do countdown timer; at the end, call doRound() to kick off the game
    }

    private void stopCountdown() {
        //reset countdown timer
    }

    /**
     * Win code here
     */
    private void doVictory() {

    }

    /**
     * Loss code here
     */
    private void doLoss() {

    }
}