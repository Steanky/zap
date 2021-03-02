package io.github.zap.zombies.game.data.powerups.spawnrules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class SpawnRuleData {
    @Getter
    private final String name;

    @Getter
    private final String type;

    @Getter
    private final String spawnRuleType;
}
