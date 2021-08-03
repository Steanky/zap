package io.github.zap.arenaapi.nms.v1_16_R3.entity;

import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.MobNavigator_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.PathEntityWrapper_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.pathfind.PathPointWrapper_v1_16_R3;
import io.github.zap.vector.Vector3I;
import net.minecraft.server.v1_16_R3.AttributeBase;
import net.minecraft.server.v1_16_R3.AttributeMapBase;
import net.minecraft.server.v1_16_R3.AttributeModifiable;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityItem;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityPiglin;
import net.minecraft.server.v1_16_R3.EntitySkeletonAbstract;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumInteractionResult;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.ItemBow;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.Navigation;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import net.minecraft.server.v1_16_R3.PathType;
import net.minecraft.server.v1_16_R3.PathfinderGoalBowShoot;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_16_R3.ProjectileHelper;
import net.minecraft.server.v1_16_R3.WorldAccess;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityBridge_v1_16_R3 implements EntityBridge {
    public static final EntityBridge_v1_16_R3 INSTANCE = new EntityBridge_v1_16_R3();
    private static final Field navigator;

    static {
        Field nav;
        try {
            nav = EntityInsentient.class.getDeclaredField("navigation");
            nav.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            nav = null;
        }

        navigator = nav;
    }

    private EntityBridge_v1_16_R3() { }

    @Override
    public int nextEntityID() {
        return net.minecraft.server.v1_16_R3.Entity.nextEntityId();
    }

    @Override
    public @NotNull UUID randomUUID() {
        return MathHelper.a(net.minecraft.server.v1_16_R3.Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeID(@NotNull EntityType type) {
        return (EntityTypes.getByName(type.getKey().getKey())).map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }

    @Override
    public @NotNull PathEntityWrapper makePathEntity(@NotNull List<PathPointWrapper> pointWrappers,
                                                     @NotNull Vector3I destination, boolean reachesDestination) {
        List<PathPoint> points = new ArrayList<>();

        for(PathPointWrapper wrapper : pointWrappers) {
            PathPointWrapper_v1_16_R3 specific = (PathPointWrapper_v1_16_R3)wrapper;
            points.add(specific.pathPoint());
        }

        return new PathEntityWrapper_v1_16_R3(new PathEntity(points, new BlockPosition(destination.x(), destination.y(),
                destination.z()), reachesDestination));
    }

    @Override
    public @NotNull PathPointWrapper makePathPoint(@NotNull Vector3I blockLocation) {
        PathPoint pathPoint = new PathPoint(blockLocation.x(), blockLocation.y(), blockLocation.z());
        pathPoint.l = PathType.WALKABLE;
        return new PathPointWrapper_v1_16_R3(pathPoint);
    }

    @Override
    public @NotNull MobNavigator overrideNavigatorFor(@NotNull Mob mob) throws IllegalAccessException {
        EntityInsentient entityInsentient = ((CraftMob)mob).getHandle();
        Navigation navigation = (Navigation) navigator.get(entityInsentient);

        if(navigation instanceof MobNavigator_v1_16_R3 mobNavigator) {
            return mobNavigator;
        }
        else {
            MobNavigator_v1_16_R3 customNavigator = new MobNavigator_v1_16_R3(entityInsentient, entityInsentient.getWorld());
            navigator.set(entityInsentient, customNavigator);
            return customNavigator;
        }
    }

    @Override
    public double distanceTo(@NotNull Entity entity, double x, double y, double z) {
        return ((CraftEntity) entity).getHandle().h(x, y, z);
    }

    @Override
    public @NotNull Random getRandomFor(@NotNull LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().getRandom();
    }

    @Override
    public boolean canSee(@NotNull Mob mob, @NotNull Entity target) {
        return ((CraftMob) mob).getHandle().getEntitySenses().a((net.minecraft.server.v1_16_R3.Entity) target);
    }

    @Override
    public void setLookDirection(@NotNull Mob mob, @NotNull Entity target, float maxYawChange, float maxPitchChange) {
        ((CraftMob) mob).getHandle().a(((CraftEntity) target).getHandle(), maxYawChange, maxPitchChange);
    }

    @Override
    public boolean hasAttribute(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute) {
        return ((CraftLivingEntity) livingEntity).getHandle().getAttributeMap()
                .b(CraftAttributeMap.toMinecraft(attribute));
    }

    @Override
    public void setAttributeFor(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute, double value) {
        EntityLiving nmsLivingEntity = ((CraftLivingEntity) livingEntity).getHandle();
        AttributeBase nmsAttribute = CraftAttributeMap.toMinecraft(attribute);

        AttributeMapBase attributeMap = nmsLivingEntity.getAttributeMap();
        AttributeModifiable modifiableAttribute = attributeMap.a(nmsAttribute);

        if (modifiableAttribute != null) {
            modifiableAttribute.setValue(value);
        }
        else {
            attributeMap.registerAttribute(nmsAttribute);
            //noinspection ConstantConditions
            attributeMap.a(nmsAttribute).setValue(value);
        }
    }

}
