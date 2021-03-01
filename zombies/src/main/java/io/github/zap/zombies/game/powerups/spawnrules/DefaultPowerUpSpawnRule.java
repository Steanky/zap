package io.github.zap.zombies.game.powerups.spawnrules;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WaveData;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;
import java.util.stream.Collectors;

@SpawnRuleType(getName = "Default")
public class DefaultPowerUpSpawnRule extends PowerUpSpawnRule<DefaultPowerUpSpawnRuleData> implements Disposable {
    public DefaultPowerUpSpawnRule(String spawnTargetName, DefaultPowerUpSpawnRuleData data, ZombiesArena arena) {
        super(spawnTargetName, data, arena);
        getArena().getEntityDeathEvent().registerHandler(this::onMobDeath);
    }

    private boolean isRound;
    private WaveData chosenWave;
    // The zombies died at this death count will drop power up
    private int deathCountUntilDrops;
    private int roundDeathCount;
    private Random random = new Random();

    private void onMobDeath(EntityDeathEvent e) {
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
            if(waveMeta.size() > 1 && waveMeta.get(0).value() == chosenWave) {
                if(deathCountUntilDrops == roundDeathCount) {
                    spawn(e.getEntity().getLocation());
                }

                roundDeathCount++;
            }
        }

    }

    private void chooseLuckyZombie(int currentRound) {
        var waves = getArena().getMap().getRounds().get(currentRound).getWaves();
        var waveCount = waves.size();
        var list = getData().getWaves().stream().filter(x -> x <= waveCount).collect(Collectors.toList());
        chosenWave = waves.get(list.get(random.nextInt(list.size())));
        final MutableInt waveMobCount = new MutableInt(0);
        waves.stream().flatMap(x -> x.getSpawnEntries().stream()).forEach(x -> waveMobCount.add(x.getMobCount()));
        deathCountUntilDrops = random.nextInt(waveMobCount.getValue());
    }

    @Override
    public void dispose() {
        getArena().getEntityDeathEvent().removeHandler(this::onMobDeath);
    }
}
