package io.github.zap.zombies.game;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something that can be a source of damage toward an ActiveMob (such as a player, or possibly the Arena)
 */
public interface DamageSource {
    /**
     * Called after damage is dealt to the specified ActiveMob.
     * @param damaged The mob that was damaged
     * @param damageAmount The amount that the mob was damaged
     * @param ignoreArmor Whether or not the damage was armor-bypassing
     */
    void onDamageDealt(@NotNull ActiveMob damaged, double damageAmount, boolean ignoreArmor);
}
