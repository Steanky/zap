package io.github.zap.zombies.game.powerups.spawnrules;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpawnRuleType {
    String getName();
}
