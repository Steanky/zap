package io.github.zap.zombies.game.data.powerups.spawnrules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
public class SpawnRuleData {
    private String name;

    private String type;

    private String spawnRuleType;
}
