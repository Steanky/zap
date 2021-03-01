package io.github.zap.zombies.game.powerups.spawnrules;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Set;

public class DefaultPowerUpSpawnRuleData extends SpawnRuleData {
    @Getter
    public Set<Integer> pattern;

    @Getter
    public Set<Integer> waves;

    public DefaultPowerUpSpawnRuleData(String name, String type, String spawnRuleType, Set<Integer> pattern, Set<Integer> waves) {
        super(name, type, spawnRuleType);
        this.pattern = pattern;
        this.waves = waves;
    }
}
