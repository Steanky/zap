package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;

@MythicMechanic(
        name = "summonMount",
        description = "Summons a mount, which is added to the current Zombies game."
)
public class SummonMountMechanic extends ZombiesArenaSkill {
    public SummonMountMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
    }

    @Override
    public boolean cast(SkillMetadata metadata, ZombiesArena arena) {
        return false;
    }
}
