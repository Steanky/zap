package io.github.zap.zombies.game.mob.mechanic;

import com.google.common.collect.ImmutableList;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

@MythicMechanic(
        name = "stealCoins",
        description = "Steals coins from nearby zombies players."
)
public class StealCoinsMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private static final Random RNG = new Random();

    private final int stealMax;
    private final int stealMin;

    public StealCoinsMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        stealMin = mlc.getInteger("stealMin",10);
        stealMax = mlc.getInteger("stealMax", 100);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        AbstractEntity caster = skillMetadata.getCaster().getEntity();
        List<MetadataValue> metadata = caster.getBukkitEntity().getMetadata(Zombies.ARENA_METADATA_NAME);

        for(MetadataValue value : metadata) {
            if(value.getOwningPlugin() == Zombies.getInstance()) {
                ZombiesArena arena = (ZombiesArena)value.value();

                if(arena != null) {
                    ZombiesPlayer zombiesPlayer = arena.getPlayerMap().get(target.getUniqueId());

                    if(zombiesPlayer != null) {
                        zombiesPlayer.setCoins(zombiesPlayer.getCoins() - (stealMin + RNG.nextInt(stealMax - stealMin)));
                    }
                }

                break;
            }
        }

        return false;
    }
}
