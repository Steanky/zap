package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.proxy.NavigationProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.Optional;

@MythicAIGoal (
        name = "unboundedMeleeAttack"
)
public class MeleeAttack extends Pathfinder implements PathfindingGoal {
    private final NavigationProxy navigationProxy;
    private final EntityCreature nmsEntity;

    private boolean metadataLoaded;

    private final int attackTicks;
    private final double attackReach;

    private ZombiesArena arena;
    private ZombiesPlayer targetPlayer;

    private int attackTimer;
    private int pathfindTimer;

    public MeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        this.goalType = GoalType.MOVE_LOOK;

        attackTicks = mlc.getInteger("attackTicks", 20);
        attackReach = mlc.getDouble("attackReach", 2.0);

        navigationProxy = Zombies.getInstance().getNmsProxy().getNavigationFor((Mob)entity.getBukkitEntity());
        nmsEntity = ((CraftCreature)entity.getBukkitEntity()).getHandle();
        nmsEntity.getAttributeMap().a(GenericAttributes.FOLLOW_RANGE).setValue(Integer.MAX_VALUE);

        /*
        randomize the rate at which this zombie recalculates its path, to make sure there are no massive lag spikes if
        hundreds of zombies recalculate at once
         */
        pathfindTimer = nmsEntity.getRandom().nextInt(20);
    }

    private boolean loadMetadata() {
        Optional<Object> arenaOptional = entity.getMetadata(Zombies.ARENA_METADATA_NAME);
        Optional<Object> spawnpointOptional = entity.getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);

        if(arenaOptional.isPresent() && spawnpointOptional.isPresent()) {
            arena = (ZombiesArena) arenaOptional.get();
            return true;
        }
        else {
            arena = null;
            return false;
        }
    }

    @Override
    public boolean shouldStart() {
        if(!metadataLoaded) {
            metadataLoaded = loadMetadata();

            if(!metadataLoaded) {
                return false;
            }
        }

        if(targetPlayer == null) {
            targetPlayer = navigationProxy.findClosest(arena, ZombiesPlayer::isAlive);
        }

        return targetPlayer != null;
    }

    @Override
    public void start() {
        ai().setTarget((LivingEntity) entity.getBukkitEntity(), targetPlayer.getPlayer());
    }

    @Override
    public void tick() {
        EntityLiving target = nmsEntity.getGoalTarget();
        nmsEntity.getControllerLook().a(target, 30.0F, 30.0F);

        if(++pathfindTimer == 20) {
            ai().navigateToLocation(entity, BukkitAdapter.adapt(targetPlayer.getPlayer().getLocation()), 0);
            pathfindTimer = 0;
        }

        this.attackTimer = Math.max(this.attackTimer - 1, 0);
        this.tryAttack(target, nmsEntity.h(target.locX(), target.locY(), target.locZ()));
    }

    @Override
    public boolean shouldEnd() {
        return targetPlayer == null || !targetPlayer.isAlive() || !arena.runAI();
    }

    @Override
    public void end() {
        if(targetPlayer != null && !targetPlayer.isAlive()) {
            targetPlayer = null;
        }
    }

    private void tryAttack(EntityLiving target, double distance) {
        if (distance <= attackDistance(target) && attackTimer <= 0) {
            resetAttackTimer();
            nmsEntity.swingHand(EnumHand.MAIN_HAND);
            nmsEntity.attackEntity(target);
        }
    }

    private double attackDistance(EntityLiving target) {
        return (nmsEntity.getWidth() * attackReach * nmsEntity.getWidth() * attackReach + target.getWidth());
    }

    private void resetAttackTimer() {
        attackTimer = attackTicks;
    }
}
