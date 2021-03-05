package io.github.zap.zombies.game.powerups.spawnrules;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.WaveData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.spawnrules.DefaultPowerUpSpawnRuleData;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Location;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The default spawnrule that similar to Hypixel
 */
@SpawnRuleType(getName = "Default")
public class DefaultPowerUpSpawnRule extends PowerUpSpawnRule<DefaultPowerUpSpawnRuleData> implements Disposable {
    public DefaultPowerUpSpawnRule(String spawnTargetName, DefaultPowerUpSpawnRuleData data, ZombiesArena arena) {
        super(spawnTargetName, data, arena);
        getArena().getMythicMobDeathEvent().registerHandler(this::onMobDeath);

        // Avoid spawning stuff inside windows
        windows = getArena().getMap().getRooms().stream().flatMap(x -> x.getWindows().stream()).collect(Collectors.toSet());
    }

    private final Set<WindowData> windows;

    private boolean isRound;
    private WaveData chosenWave;
    // The zombies died at this death count will drop power up
    private int deathCountUntilDrops;
    private int roundDeathCount;
    private Random random = new Random();

    private void onMobDeath(MythicMobDeathEvent e) {
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
            var waveMeta = e.getEntity().getMetadata(Zombies.SPAWNINFO_WAVE_METADATA_NAME);
            if(waveMeta.size() == 1 && ((FixedMetadataValue) waveMeta.get(0).value()).value() == chosenWave) {
                if(deathCountUntilDrops == roundDeathCount && !isDisabledRound()) {
                    spawn(getSuitableLocation(e.getEntity().getLocation()));
                }

                roundDeathCount++;
                System.out.println("z" + roundDeathCount + "/" + deathCountUntilDrops);
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

    private Location getSuitableLocation(Location location) {
        for(var window : windows) {
            if(window.getFaceBounds().overlaps(location.toVector(), location.toVector()) ||
                    window.getInteriorBounds().contains(location.toVector())) {
                var newSpawnVec = window.getTarget();
                return new Location(location.getWorld(), newSpawnVec.getX(), newSpawnVec.getY(), newSpawnVec.getZ());
            }
        }

        return location;
    }


    @Override
    public void dispose() {
        getArena().getMythicMobDeathEvent().removeHandler(this::onMobDeath);
    }
}
