package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.jetbrains.annotations.NotNull;

@MythicMechanic(
        name = "slowFireRate",
        description = "Slows the fire rate of the target player by a configurable amount."
)
public class SlowFireRateMechanic extends ZombiesPlayerSkill {
    private static final String SLOW_FIRE_RATE_MODIFIER_NAME = "zz_slow_low_iq";

    public final double speedModifier;

    public SlowFireRateMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        speedModifier = mlc.getDouble("speedModifier", 0.5);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena, @NotNull ZombiesPlayer target) {
        target.getFireRateMultiplier().registerModifier(SLOW_FIRE_RATE_MODIFIER_NAME, d -> d == null ? 0D : d * speedModifier);
        return true;
    }
}
