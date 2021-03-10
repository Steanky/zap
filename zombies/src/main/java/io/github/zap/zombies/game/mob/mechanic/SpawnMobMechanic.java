package io.github.zap.zombies.game.mob.mechanic;

import com.google.common.collect.ImmutableList;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

@MythicMechanic(
        name = "spawnMobs",
        description = "General skill used for spawning mobs in Zombies games."
)
public class SpawnMobMechanic extends SkillMechanic implements INoTargetSkill, Listener {
    private static final String OWNER_METADATA_NAME = "spawn_owner";

    private final String mobType;
    private final int mobCountMin;
    private final int mobCountMax;
    private final int mobCap;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadiusSquared;

    private static final Map<UUID, Set<UUID>> mobs = new HashMap<>();
    private static final Random RNG = new Random();

    public SpawnMobMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        super.setAsyncSafe(false);
        mobType = mlc.getString("mobType");
        mobCountMin = mlc.getInteger("mobCountMin", 1);
        mobCountMax = mlc.getInteger("mobCountMax", 1);
        mobCap = mlc.getInteger("mobCap", 5);
        useSpawnpoints = mlc.getBoolean("useSpawnpoints", false);
        ignoreSpawnrule = mlc.getBoolean("ignoreSpawnrule", false);
        spawnRadiusSquared = mlc.getDouble("spawnRadiusSquared", 4096);

        Zombies.getInstance().getServer().getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        AbstractEntity caster = skillMetadata.getCaster().getEntity();
        List<MetadataValue> metadata = caster.getBukkitEntity().getMetadata(Zombies.ARENA_METADATA_NAME);
        for(MetadataValue value : metadata) {
            if(value.getOwningPlugin() == Zombies.getInstance()) {
                ZombiesArena arena = (ZombiesArena)value.value();

                if(arena != null) {
                    Set<UUID> spawnedMobs = mobs.computeIfAbsent(caster.getUniqueId(), dummy -> new HashSet<>());

                    if(spawnedMobs.size() < mobCap) {
                        int cap = mobCap - spawnedMobs.size();
                        int spawnAmount = Math.min(cap, mobCountMin + RNG.nextInt(mobCountMax - mobCountMin));

                        if(useSpawnpoints) {
                            List<ActiveMob> spawned = arena.getSpawner().spawnMobs(ImmutableList.of(new SpawnEntryData(mobType, spawnAmount)),
                                    ignoreSpawnrule ? SpawnMethod.IGNORE_SPAWNRULE : SpawnMethod.RANGED, spawnRadiusSquared, true);

                            for(ActiveMob mob : spawned) {
                                spawnedMobs.add(mob.getUniqueId());
                            }
                        }
                        else {
                            for(int i = 0; i < spawnAmount; i++) {
                                ActiveMob mob = arena.getSpawner().spawnAt(mobType, skillMetadata.getCaster().getEntity().getBukkitEntity()
                                        .getLocation().toVector());

                                if(mob != null) {
                                    spawnedMobs.add(mob.getUniqueId());
                                }
                            }
                        }

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    @EventHandler
    private void onMythicMobDeath(MythicMobDeathEvent event) {
        UUID deadUUID = event.getEntity().getUniqueId();

        if(mobs.remove(deadUUID) == null) {
            List<MetadataValue> values = event.getEntity().getMetadata(OWNER_METADATA_NAME);
            for(MetadataValue value : values) {
                if(value.getOwningPlugin() == MythicMobs.inst()) {
                    UUID ownerId = (UUID) value.value();

                    if(ownerId != null) {
                        Set<UUID> spawned = mobs.get(ownerId);
                        if(spawned != null) {
                            spawned.remove(deadUUID);

                            if(spawned.size() == 0) {
                                mobs.remove(ownerId);
                            }
                        }
                    }

                    break;
                }
            }
        }
    }
}
