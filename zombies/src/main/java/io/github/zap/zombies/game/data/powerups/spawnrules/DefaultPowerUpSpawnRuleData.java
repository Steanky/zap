package io.github.zap.zombies.game.data.powerups.spawnrules;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DefaultPowerUpSpawnRuleData extends SpawnRuleData {
    public Set<Integer> pattern;

    public Set<Integer> waves;
}
