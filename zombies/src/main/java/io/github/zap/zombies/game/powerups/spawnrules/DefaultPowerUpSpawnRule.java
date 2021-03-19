package io.github.zap.zombies.game.powerups.spawnrules;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.WaveData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.spawnrules.DefaultPowerUpSpawnRuleData;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The default spawnrule that similar to Hypixel
 */
@SpawnRuleType(getName = "Default")
public class DefaultPowerUpSpawnRule extends PowerUpSpawnRule<DefaultPowerUpSpawnRuleData> implements Disposable {
    public DefaultPowerUpSpawnRule(String spawnTargetName, DefaultPowerUpSpawnRuleData data, ZombiesArena arena) {
        super(spawnTargetName, data, arena);
        getArena().getProxyFor(EntityRemoveFromWorldEvent.class).registerHandler(this::onMobDeath);

        // Avoid spawning stuff inside windows
        windows = getArena().getMap().getRooms().stream().flatMap(x -> x.getWindows().stream()).collect(Collectors.toList());
    }

    //changed this to a List (set is slow to iterate and you never call .contains which means list is optimal)
    private final List<WindowData> windows;

    private boolean isRound;
    private WaveData chosenWave;
    // The zombies died at this death count will drop power up
    private int deathCountUntilDrops;
    private int roundDeathCount;
    private final Random random = new Random();

    private boolean disposed = false;

    private void onMobDeath(ZombiesArena.ProxyArgs<EntityRemoveFromWorldEvent> e) {
        var patterns = getData().getPattern();
        var currentRound = getArena().getMap().getCurrentRoundProperty().getValue(getArena());
        if(patterns.contains(currentRound)) {
            if(!isRound) {
                isRound = true;
                chooseLuckyZombie(currentRound);
            }
        } else {
            isRound = false;
        }

        if(isRound) {
            //using new MetadataHelper util class; the old code would have failed if another plugin happened to register metadata to that entity
            WaveData waveData = MetadataHelper.getMetadataFor(e.getEvent().getEntity(), Zombies.getInstance(), Zombies.SPAWNINFO_WAVE_METADATA_NAME);

            if(waveData == chosenWave) {
                if(deathCountUntilDrops == roundDeathCount && !isDisabledRound()) {
                    spawn(getSuitableLocation(e.getEvent().getEntity()));
                }

                roundDeathCount++;
            }
        }

    }

    private void chooseLuckyZombie(int currentRound) {
        var waves = getArena().getMap().getRounds().get(currentRound - 1).getWaves();
        var waveCount = waves.size();
        var list = getData().getWaves().stream().filter(x -> x <= waveCount).collect(Collectors.toList());
        chosenWave = waves.get(list.get(random.nextInt(list.size())) - 1);
        final MutableInt waveMobCount = new MutableInt(0);
        chosenWave.getSpawnEntries().stream().map(SpawnEntryData::getMobCount).forEach(waveMobCount::add);
        deathCountUntilDrops = random.nextInt(waveMobCount.getValue());
    }

    private Location getSuitableLocation(Entity entity) {
        for(var window : windows) {
            if(window.getFaceBounds().overlaps(entity.getBoundingBox()) ||
                    window.getInteriorBounds().overlaps(entity.getBoundingBox())) {
                var newSpawnVec = window.getTarget();
                return newSpawnVec.toLocation(entity.getWorld());
            }
        }

        return entity.getLocation();
    }


    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        getArena().getProxyFor(EntityRemoveFromWorldEvent.class).removeHandler(this::onMobDeath);
        disposed = true;
    }
}
