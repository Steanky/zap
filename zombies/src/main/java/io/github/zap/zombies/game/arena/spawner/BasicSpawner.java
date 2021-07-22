package io.github.zap.zombies.game.arena.spawner;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.arena.event.EntityArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.RoundContext;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.RoundData;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WaveData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Basic implementation of a {@link Spawner}
 */
public class BasicSpawner implements Spawner {

    private final @NotNull Set<@NotNull Mob> mobs = new HashSet<>();

    private final @NotNull World world;

    private final @NotNull MapData map;

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull BukkitTaskManager taskManager;

    private final @NotNull Event<@NotNull ZombieCountChangedArgs> zombieCountChangedEvent = new Event<>();

    private int zombiesLeft;

    public BasicSpawner(@NotNull World world, @NotNull MapData map,
                        @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList,
                        @NotNull BukkitTaskManager taskManager) {
        this.world = world;
        this.map = map;
        this.playerList = playerList;
        this.taskManager = taskManager;
    }

    public void spawnRound(@NotNull RoundData round) {
        RoundContext context = new RoundContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        long cumulativeDelay = 0;
        zombiesLeft = 0;
        for (WaveData wave : round.getWaves()) {
            cumulativeDelay += wave.getWaveLength();

            BukkitTask waveSpawnTask = taskManager.runTaskLater(cumulativeDelay, () -> {
                context.spawnedMobs().addAll(spawnMobs(wave.getSpawnEntries(), wave.getMethod(), wave.getSlaSquared(),
                        wave.isRandomizeSpawnpoints(), false));

                for (ActiveMob activeMob : context.spawnedMobs()) {
                    MetadataHelper.setMetadataFor(activeMob.getEntity().getBukkitEntity(),
                            Zombies.SPAWNINFO_WAVE_METADATA_NAME, Zombies.getInstance(), wave);
                }

                BukkitTask removeMobTask = taskManager.runTaskLater(6000, () -> {
                    for (ActiveMob mob : context.spawnedMobs()) {
                        Entity entity = mob.getEntity().getBukkitEntity();

                        if(entity != null) {
                            entity.remove();
                        }
                    }
                });

                context.removeTasks().add(removeMobTask);
            });

            context.spawnTasks().add(waveSpawnTask);

            for (SpawnEntryData spawnEntryData : wave.getSpawnEntries()) {
                zombiesLeft += spawnEntryData.getMobCount();
            }
        }

        roundIndexProperty.setValue(this, targetRound);

        for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
            Component title = round.getCustomMessage() != null && !round.getCustomMessage().isEmpty()
                    ? Component.text(currentRound.getCustomMessage())
                    : Component.text("ROUND " + (targetRound + 1), NamedTextColor.RED);
            player.getPlayer().showTitle(Title.title(title, Component.empty()));
            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.wither.spawn"), Sound.Source.MASTER,
                    1.0F, 0.5F));
        }

        if (map.getDisablePowerUpRound().contains(targetRound + 1)) {
            var items = getPowerUps().stream()
                    .filter(x -> x.getState() == PowerUpState.NONE || x.getState() == PowerUpState.DROPPED)
                    .collect(Collectors.toSet());
            items.forEach(PowerUp::deactivate);
        }
    }

    @Override
    public @NotNull List<@NotNull ActiveMob> spawnMobs(@NotNull List<@NotNull SpawnEntryData> mobs,
                                                       @NotNull SpawnMethod method,
                                                       @NotNull Predicate<SpawnpointData> filter, double slaSquared,
                                                       boolean randomize, boolean updateCount) {
        List<@NotNull SpawnContext> spawns = filterSpawnpoints(mobs, method, filter, slaSquared);
        List<ActiveMob> spawnedEntities = new ArrayList<>();

        if (spawns.isEmpty()) {
            Zombies.warning("There are no available spawnpoints for this mob set. This likely indicates an error " +
                    "in map configuration.");
            return Collections.emptyList();
        }

        if (randomize) {
            Collections.shuffle(spawns); //shuffle small candidate set of spawnpoints
        }

        for (SpawnEntryData spawnEntryData : mobs) {
            int amount = spawnEntryData.getMobCount();

            outer:
            while(true) {
                int startingAmount = amount;
                for (SpawnContext context : spawns) {
                    if (method == SpawnMethod.IGNORE_SPAWNRULE
                            || context.spawnpoint().canSpawn(spawnEntryData.getMobName(), map)) {
                        ActiveMob mob = spawnMob(spawnEntryData.getMobName(), context.spawnpoint().getSpawn());

                        if (mob != null) {
                            spawnedEntities.add(mob);
                            MetadataHelper.setMetadataFor(mob.getEntity().getBukkitEntity(),
                                    Zombies.WINDOW_METADATA_NAME, Zombies.getInstance(), context.window);

                            if (updateCount) {
                                zombiesLeft++;
                            }
                        }
                        else {
                            // mob failed to spawn; if we're part of a wave, reduce the amt of zombies
                            if (!updateCount) {
                                zombieCountChangedEvent.callEvent(new ZombieCountChangedArgs(zombiesLeft--,
                                        zombiesLeft));
                            }

                            Zombies.warning("Mob failed to spawn!");
                        }

                        if (--amount == 0) {
                            break outer;
                        }
                    }
                }

                if (startingAmount == amount) { // make sure we managed to spawn at least one mob
                    Zombies.warning("Unable to find a valid spawnpoint for SpawnEntryData.");

                    if (!updateCount) { // reduce zombie count if none spawned due to no windows being in range
                        zombieCountChangedEvent.callEvent(new ZombieCountChangedArgs(zombiesLeft,
                                zombiesLeft -= amount));
                    }
                    break;
                }
            }
        }

        return spawnedEntities;
    }

    @Override
    public ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount) {
        ActiveMob spawned = spawnMob(mobType, vector);

        if (spawned != null) {
            if (updateCount) {
                zombiesLeft++;
            }

            RoomData roomIn = map.roomAt(vector);
            if (roomIn != null) {
                for (WindowData window : roomIn.getWindows()) {
                    if (window.playerInside(vector)) {
                        MetadataHelper.setMetadataFor(spawned.getEntity().getBukkitEntity(),
                                Zombies.WINDOW_METADATA_NAME, Zombies.getInstance(), window);
                        break;
                    }
                }
            }
        }

        return spawned;
    }

    @Override
    public @NotNull Set<@NotNull Mob> getMobs() {
        return Collections.unmodifiableSet(mobs);
    }

    @Override
    public int getZombiesLeft() {
        return zombiesLeft;
    }

    @Override
    public @NotNull Event<@NotNull ZombieCountChangedArgs> getZombieCountChangedEvent() {
        return zombieCountChangedEvent;
    }

    @Override
    public void onMobDeath(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityDeathEvent> args) {
        if (mobs.remove(args.entity())) {
            zombieCountChangedEvent.callEvent(new ZombieCountChangedArgs(zombiesLeft--, zombiesLeft));
        }
    }

    @Override
    public void onMobRemoveFromWorld(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityRemoveFromWorldEvent> args) {
        if (mobs.remove(args.entity())) {
            zombieCountChangedEvent.callEvent(new ZombieCountChangedArgs(zombiesLeft--, zombiesLeft));
        }
    }

    private ActiveMob spawnMob(String mobName, Vector blockPosition) {
        MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);

        if (mob != null) {
            ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), blockPosition.getX() +
                    0.5, blockPosition.getY(), blockPosition.getZ() + 0.5), map.getMobSpawnLevel());

            if (activeMob != null) {
                mobs.add((Mob) activeMob.getEntity().getBukkitEntity());
                MetadataHelper.setMetadataFor(activeMob.getEntity().getBukkitEntity(), Zombies.ARENA_METADATA_NAME,
                        Zombies.getInstance(), ZombiesArena.this);

                return activeMob;
            }
            else {
                Zombies.warning(String.format("An error occurred while trying to spawn mob of type '%s'.", mobName));
            }
        }
        else {
            Zombies.warning(String.format("Mob type '%s' is not known.", mobName));
        }

        return null;
    }

    private List<@NotNull SpawnContext> filterSpawnpoints(@NotNull List<@NotNull SpawnEntryData> mobs,
                                                          @NotNull SpawnMethod method,
                                                          @NotNull Predicate<@NotNull SpawnpointData> filter,
                                                          double slaSquared) {
        List<@NotNull SpawnContext> filtered = new ArrayList<>();

        for (RoomData room : map.getRooms()) { //iterate rooms
            if (room.isSpawn() || method == SpawnMethod.FORCE
                    || room.getOpenProperty().getValue(ZombiesArena.this)) {
                // add all valid spawnpoints in the room
                addValidContext(filtered, room.getSpawnpoints(), mobs, method, filter, slaSquared, null);

                for (WindowData window : room.getWindows()) { //check window spawnpoints next
                    addValidContext(filtered, window.getSpawnpoints(), mobs, method, filter, slaSquared, window);
                }
            }
        }

        return filtered;
    }

    private void addValidContext(@NotNull List<@NotNull SpawnContext> addTo, @NotNull List<@NotNull SpawnpointData> spawnpoints,
                                 @NotNull List<@NotNull SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                 @NotNull Predicate<@NotNull SpawnpointData> filter, double slaSquared,
                                 @Nullable WindowData window) {
        for (@NotNull SpawnpointData spawnpointData : spawnpoints) {
            if (filter.test(spawnpointData) && canSpawnAny(spawnpointData, mobs, method, slaSquared)) {
                addTo.add(new SpawnContext(spawnpointData, window));
            }
        }
    }

    private boolean canSpawnAny(@NotNull SpawnpointData spawnpoint, @NotNull List<@NotNull SpawnEntryData> entry,
                                @NotNull SpawnMethod method, double slaSquared) {
        if (method == SpawnMethod.IGNORE_SPAWNRULE) {
            return checkSLA(spawnpoint, slaSquared);
        }
        else {
            for (SpawnEntryData data : entry) {
                if (spawnpoint.canSpawn(data.getMobName(), map)) {
                    return method != SpawnMethod.RANGED || checkSLA(spawnpoint, slaSquared);
                }
            }
        }

        return false;
    }

    private boolean checkSLA(@NotNull SpawnpointData target, double slaSquared) {
        for (@NotNull ZombiesPlayer player : playerList.getOnlinePlayers()) {
            if (player.getPlayer().getLocation().toVector().distanceSquared(target.getSpawn()) <= slaSquared) {
                return true;
            }
        }

        return false;
    }

    private record SpawnContext(@NotNull SpawnpointData spawnpoint, @Nullable WindowData window) {

    }

}
