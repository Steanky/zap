package io.github.zap.arenaapi.nms.v1_17_R1.entity;

import io.github.zap.zombies.nms.common.entity.EntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMob;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class EntityBridge_v1_17_R1 implements EntityBridge {

    public static final EntityBridge_v1_17_R1 INSTANCE = new EntityBridge_v1_17_R1();

    @Override
    public boolean replacePersistentGoals(@NotNull Mob mob) {
        if (((CraftMob) mob).getHandle() instanceof net.minecraft.world.entity.monster.AbstractSkeleton skeleton) {
            try {
                Field bowShootGoal = net.minecraft.world.entity.monster.AbstractSkeleton.class
                        .getDeclaredField("bowGoal");
                Field meleeAttackGoal = net.minecraft.world.entity.monster.AbstractSkeleton.class
                        .getDeclaredField("meleeGoal");

                bowShootGoal.setAccessible(true);
                meleeAttackGoal.setAccessible(true);

                bowShootGoal.set(skeleton, new RangedBowAttackGoal<>(skeleton, 0, 0, 0) {
                    @Override
                    public boolean canUse() {
                        return false;
                    }
                });
                meleeAttackGoal.set(skeleton, new MeleeAttackGoal(skeleton, 0, false) {
                    @Override
                    public boolean canUse() {
                        return false;
                    }
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setAggressive(@NotNull Mob mob, boolean aggressive) {
        ((CraftMob) mob).getHandle().setAggressive(aggressive);
    }

    @Override
    public void strafe(@NotNull Mob mob, float forward, float sideways) {
        ((CraftMob) mob).getHandle().getMoveControl().strafe(forward, sideways);
    }

    @Override
    public int getTicksUsingItem(@NotNull LivingEntity livingEntity) {
        return ((CraftLivingEntity) livingEntity).getHandle().getTicksUsingItem();
    }

    @Override
    public float getCharge(int ticks) {
        return BowItem.getPowerForTime(ticks);
    }

    @Override
    public void startPullingBow(@NotNull LivingEntity livingEntity) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = ((CraftLivingEntity) livingEntity).getHandle();
        nmsLivingEntity.startUsingItem(ProjectileUtil.getWeaponHoldingHand(nmsLivingEntity, Items.BOW));
    }

    @Override
    public boolean isAbstractSkeleton(@NotNull Entity entity) {
        return entity instanceof AbstractSkeleton;
    }

    @Override
    public @NotNull Piglin makeDream(@NotNull World world) {
        return (Piglin) new net.minecraft.world.entity.monster.piglin.Piglin(net.minecraft.world.entity.EntityType.PIGLIN,
                ((CraftWorld) world).getHandle()) {
            {
                setInvulnerable(true);
                setPersistenceRequired();
                setNoAi(true);
            }

            @javax.annotation.Nullable
            @Override
            public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                                MobSpawnType spawnReason,
                                                @javax.annotation.Nullable SpawnGroupData entityData,
                                                @javax.annotation.Nullable CompoundTag entityNbt) {
                return null;
            }

            @Override
            protected void populateDefaultEquipmentSlots(DifficultyInstance difficultydamagescaler) {

            }

            @Override
            public InteractionResult mobInteract(Player player, InteractionHand hand) {
                return InteractionResult.PASS;
            }

            @Override
            protected void customServerAiStep() {

            }

            @Override
            public boolean hurt(DamageSource damagesource, float f) {
                return false;
            }

            @Override
            protected void pickUpItem(ItemEntity entityitem) {

            }

            @Override
            public boolean isPushable() {
                return false;
            }
        }.getBukkitEntity();
    }

    @Override
    public void spawnDream(@NotNull Piglin dream, @NotNull World world) {
        ((CraftWorld) world).addEntity(((CraftEntity) dream).getHandle(), CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

}
