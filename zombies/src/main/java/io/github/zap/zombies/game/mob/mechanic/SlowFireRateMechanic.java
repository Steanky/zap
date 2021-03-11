package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.metadata.MetadataValue;
import java.util.List;

@MythicMechanic(
        name = "slowFireRate",
        description = "Slows the fire rate of the target player by a configurable amount."
)
public class SlowFireRateMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private static final String MODIFIER_NAME = "slow_fire_rate_skill";

    public final double speedModifier;

    public SlowFireRateMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        speedModifier = mlc.getDouble("speedModifier", 0.5);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        AbstractEntity caster = skillMetadata.getCaster().getEntity();
        List<MetadataValue> metadata = caster.getBukkitEntity().getMetadata(Zombies.ARENA_METADATA_NAME);
        for(MetadataValue value : metadata) {
            if(value.getOwningPlugin() == Zombies.getInstance()) {
                ZombiesArena arena = (ZombiesArena)value.value();

                if(arena != null) {
                    ZombiesPlayer target = arena.getPlayerMap().get(abstractEntity.getUniqueId());

                    if(target != null) {
                        target.getFireRateMultiplier().registerModifier(MODIFIER_NAME, d -> d == null ? 0D : d * speedModifier);
                    }
                }

                break;
            }
        }

        return false;
    }
}
