package io.github.zap.zombies.game.mob.mechanic;

import com.google.common.collect.ImmutableList;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

@MythicMechanic(
        name = "spawnMobs",
        description = "General skill used for spawning mobs in Zombies games."
)
public class SpawnMobMechanic extends SkillMechanic implements INoTargetSkill {
    private final String mobType;
    private final int mobCount;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadius;

    public SpawnMobMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        super.setAsyncSafe(false);
        mobType = mlc.getString("mobType");
        mobCount = mlc.getInteger("mobCount", 1);
        useSpawnpoints = mlc.getBoolean("useSpawnpoints", false);
        spawnRadius = mlc.getDouble("spawnRadius", 0);
        ignoreSpawnrule = mlc.getBoolean("ignoreSpawnrule", false);
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        List<MetadataValue> metadata = skillMetadata.getCaster().getEntity().getBukkitEntity().getMetadata(Zombies.ARENA_METADATA_NAME);
        for(MetadataValue value : metadata) {
            if(value.getOwningPlugin() == Zombies.getInstance()) {
                ZombiesArena arena = (ZombiesArena)value.value();

                if(arena != null) {
                    if(useSpawnpoints) {
                        arena.getSpawner().spawnMobs(ImmutableList.of(new SpawnEntryData(mobType, mobCount)),
                                ignoreSpawnrule ? SpawnMethod.IGNORE_SPAWNRULE : SpawnMethod.RANGED, spawnRadius, true);
                    }
                    else {
                        for(int i = 0; i < mobCount; i++) {
                            arena.getSpawner().spawnAt(mobType, skillMetadata.getCaster().getEntity().getBukkitEntity()
                                    .getLocation().toVector());
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
