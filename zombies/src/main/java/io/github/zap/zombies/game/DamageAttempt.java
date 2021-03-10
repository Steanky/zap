package io.github.zap.zombies.game;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a specific attempt to deal damage to something.
 */
public interface DamageAttempt {
    /**
     * Gets the coins that should be awarded for this damage attempt.
     */
    int getCoins();

    /**
     * Gets the amount of damage that should be dealt.
     */
    double damageAmount();

    /**
     * Gets whether or not the damage should ignore armor.
     */
    boolean ignoresArmor();

    /**
     * Gets the vector that indicates to which direction the mob should be knocked back as a result of this DamageAttempt.
     */
    @NotNull Vector directionVector();

    /**
     * Gets the amount of knockback this DamageAttempt should deal.
     */
    double knockbackFactor();
}
