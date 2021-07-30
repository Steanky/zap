package io.github.zap.zombies.game.powerups;

import java.lang.annotation.*;

/**
 * Naming convention: Capitalize words and use dash instead of space (eg: Sb-Slow)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PowerUpType {
    String name();
}
