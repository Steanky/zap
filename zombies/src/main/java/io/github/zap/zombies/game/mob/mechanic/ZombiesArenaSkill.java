package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public abstract class ZombiesArenaSkill extends SkillMechanic implements INoTargetSkill {
    public ZombiesArenaSkill(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        ZombiesArena arena = MetadataHelper.getMetadataFor(skillMetadata.getCaster().getEntity().getBukkitEntity(),
                Zombies.getInstance(), Zombies.ARENA_METADATA_NAME);

        if(arena != null) {
            return cast(skillMetadata, arena);
        }

        return false;
    }

    public abstract boolean cast(SkillMetadata metadata, ZombiesArena arena);
}
