package io.github.zap.zombies.game.data.powerups.spawnrules;

import lombok.Getter;

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
