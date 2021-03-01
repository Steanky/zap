package io.github.zap.zombies.game.powerups.spawnrules;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Value
public class DefaultPowerUpSpawnRuleData extends SpawnRuleData {
    public Set<Integer> pattern;
    public Set<Integer> waves;
}
