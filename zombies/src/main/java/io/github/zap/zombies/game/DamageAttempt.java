package io.github.zap.zombies.game;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a thing that can damage entities in a Zombies game (a gun, melee weapon, possibly the arena itself, etc).
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

    @NotNull Vector directionVector();

    double knockbackFactor();
}
