package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.RangelessSpawner;
import io.github.zap.zombies.game.Spawner;
import io.github.zap.zombies.game.ZombiesArena;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;

@MythicMechanic(
        author = "NoVegetals",
        name = "spawnMob",
        description = "Spawns mobs in the context of a Zombies game (spawns at spawnpoints)."
)
public class SpawnMobMechanic extends SkillMechanic implements INoTargetSkill {
    private final MythicMob spawnedMob;
    private final int amountMin;
    private final int amountMax;
    private final int spawnCap;

    private final Map<ZombiesArena, List<ActiveMob>> activeSpawns = new HashMap<>();
    private final Spawner spawner = new RangelessSpawner();

    public SpawnMobMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        spawnedMob = MythicMobs.inst().getAPIHelper().getMythicMob(mlc.getString("mob"));
        amountMin = mlc.getInteger("amountMin", 0);
        amountMax = mlc.getInteger("amountMax", 69);
        spawnCap = mlc.getInteger("spawnCap", 5);
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        int amount = RandomUtils.nextInt(amountMin, amountMax);

        if(amount > 0) {
            AbstractEntity casterEntity = skillMetadata.getCaster().getEntity();
            Optional<Object> arenaMetadata = casterEntity.getMetadata(Zombies.ARENA_METADATA_NAME);
            SkillCaster caster = skillMetadata.getCaster();

            if(arenaMetadata.isPresent() && caster instanceof ActiveMob) {
                ZombiesArena arena = (ZombiesArena)arenaMetadata.get();

                activeSpawns.putIfAbsent(arena, new ArrayList<>());

                List<ActiveMob> current = activeSpawns.get(arena);
                List<MythicMob> newMobs = new ArrayList<>();

                for(int i = current.size() - 1; i >= 0; i--) {
                    if(current.get(i).isDead()) {
                        current.remove(i);
                    }
                }

                int limit = Math.min(Math.min(0, spawnCap - current.size()), amount);

                if(limit > 0) {
                    for(int i = 0; i < limit; i++) {
                        newMobs.add(spawnedMob);
                    }

                    List<ActiveMob> spawnedMobs = arena.spawnMobs(newMobs, spawner);
                    if(spawnedMobs.size() > 0) {
                        current.addAll(arena.spawnMobs(newMobs, spawner));
                    }
                }
            }
        }

        return false;
    }
}