package io.github.zap.arenaapi.v1_17_R1.entity;

import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import io.github.zap.arenaapi.v1_17_R1.pathfind.MobNavigator_v1_17_R1;
import io.github.zap.arenaapi.v1_17_R1.pathfind.PathEntityWrapper_v1_17_R1;
import io.github.zap.arenaapi.v1_17_R1.pathfind.PathPointWrapper_v1_17_R1;
import io.github.zap.vector.Vector3I;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityBridge_v1_17_R1 implements EntityBridge {
    public static final EntityBridge_v1_17_R1 INSTANCE = new EntityBridge_v1_17_R1();
    private static final Field navigator;

    static {
        Field nav;
        try {
            nav = net.minecraft.world.entity.Mob.class.getDeclaredField("navigation");
            nav.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            nav = null;
        }

        navigator = nav;
    }

    private EntityBridge_v1_17_R1() { }

    @Override
    public int nextEntityID() {
        return net.minecraft.world.entity.Entity.nextEntityId();
    }

    @Override
    public @NotNull UUID randomUUID() {
        return Mth.createInsecureUUID(net.minecraft.world.entity.Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeID(@NotNull EntityType type) {
        return (net.minecraft.world.entity.EntityType.byString(type.getKey().getKey())).map(Registry.ENTITY_TYPE::getId).orElse(-1);
    }

    @Override
    public @NotNull PathEntityWrapper makePathEntity(@NotNull List<PathPointWrapper> pointWrappers,
                                                     @NotNull Vector3I destination, boolean reachesDestination) {
        List<Node> points = new ArrayList<>();

        for(PathPointWrapper wrapper : pointWrappers) {
            PathPointWrapper_v1_17_R1 specific = (PathPointWrapper_v1_17_R1)wrapper;
            points.add(specific.pathPoint());
        }

        return new PathEntityWrapper_v1_17_R1(new Path(points, new BlockPos(destination.x(), destination.y(),
                destination.z()), reachesDestination));
    }

    @Override
    public @NotNull PathPointWrapper makePathPoint(@NotNull Vector3I blockLocation) {
        Node pathPoint = new Node(blockLocation.x(), blockLocation.y(), blockLocation.z());
        pathPoint.type = BlockPathTypes.WALKABLE;
        return new PathPointWrapper_v1_17_R1(pathPoint);
    }

    @Override
    public @NotNull MobNavigator overrideNavigatorFor(@NotNull Mob mob) throws IllegalAccessException {
        net.minecraft.world.entity.Mob entityInsentient = ((CraftMob)mob).getHandle();
        PathNavigation navigation = (PathNavigation) navigator.get(entityInsentient);

        if(navigation instanceof MobNavigator_v1_17_R1 mobNavigator) {
            return mobNavigator;
        }
        else {
            MobNavigator_v1_17_R1 customNavigator = new MobNavigator_v1_17_R1(entityInsentient, entityInsentient.level);
            navigator.set(entityInsentient, customNavigator);
            return customNavigator;
        }
    }

    @Override
    public double distanceTo(@NotNull Entity entity, double x, double y, double z) {
        return ((CraftEntity) entity).getHandle().distanceToSqr(x, y, z);
    }

    @Override
    public @NotNull Random getRandomFor(@NotNull LivingEntity livingEntity) {
        return ((CraftLivingEntity) livingEntity).getHandle().getRandom();
    }

    @Override
    public boolean canSee(@NotNull Mob mob, @NotNull Entity target) {
        return ((CraftMob) mob).getHandle().getSensing().hasLineOfSight(((CraftEntity) target).getHandle());
    }

    @Override
    public void setLookDirection(@NotNull Mob mob, @NotNull Entity target, float maxYawChange, float maxPitchChange) {
        ((CraftMob) mob).getHandle().lookAt(((CraftEntity) target).getHandle(), maxYawChange, maxPitchChange);
    }

    @Override
    public boolean hasAttribute(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute) {
        return ((CraftLivingEntity) livingEntity).getHandle().getAttributes()
                .hasAttribute((CraftAttributeMap.toMinecraft(attribute)));
    }

    @Override
    public void setAttributeFor(@NotNull LivingEntity livingEntity, @NotNull Attribute attribute, double value) {
        net.minecraft.world.entity.LivingEntity nmsLivingEntity = ((CraftLivingEntity) livingEntity).getHandle();
        net.minecraft.world.entity.ai.attributes.Attribute nmsAttribute = CraftAttributeMap.toMinecraft(attribute);

        AttributeMap attributeMap = nmsLivingEntity.getAttributes();
        AttributeInstance modifiableAttribute = attributeMap.getInstance(nmsAttribute);

        if (modifiableAttribute != null) {
            modifiableAttribute.setBaseValue(value);
        }
        else {
            attributeMap.registerAttribute(nmsAttribute);
            //noinspection ConstantConditions
            attributeMap.getInstance(nmsAttribute).setBaseValue(value);
        }
    }

}
