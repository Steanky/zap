package io.github.zap.nms.v1_16_R3.entity;

import io.github.zap.nms.common.entity.EntityBridge;
import io.github.zap.nms.common.pathfind.MobNavigator;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.nms.common.pathfind.PathPointWrapper;
import io.github.zap.nms.v1_16_R3.pathfind.MobNavigator_v1_16_R3;
import io.github.zap.nms.v1_16_R3.pathfind.PathEntityWrapper_v1_16_R3;
import io.github.zap.nms.v1_16_R3.pathfind.PathPointWrapper_v1_16_R3;
import io.github.zap.vector.VectorAccess;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityBridge_v1_16_R3 implements EntityBridge {
    public static final EntityBridge_v1_16_R3 INSTANCE = new EntityBridge_v1_16_R3();

    private EntityBridge_v1_16_R3() {}

    @Override
    public int nextEntityID() {
        return Entity.nextEntityId();
    }

    @Override
    public @NotNull UUID randomUUID() {
        return MathHelper.a(Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeID(@NotNull EntityType type) {
        return (EntityTypes.getByName(type.getKey().getKey())).map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }

    @Override
    public @NotNull PathEntityWrapper makePathEntity(@NotNull List<PathPointWrapper> pointWrappers,
                                                     @NotNull VectorAccess destination, boolean reachesDestination) {
        List<PathPoint> points = new ArrayList<>();
        for(PathPointWrapper wrapper : pointWrappers) {
            PathPointWrapper_v1_16_R3 specific = (PathPointWrapper_v1_16_R3)wrapper;
            points.add(specific.pathPoint());
        }

        return new PathEntityWrapper_v1_16_R3(new PathEntity(points, new BlockPosition(destination.blockX(), destination.blockY(),
                destination.blockZ()), reachesDestination));
    }

    @Override
    public @NotNull PathPointWrapper makePathPoint(@NotNull VectorAccess blockLocation) {
        PathPoint pathPoint = new PathPoint(blockLocation.blockX(), blockLocation.blockY(), blockLocation.blockZ());
        pathPoint.l = PathType.WALKABLE;
        return new PathPointWrapper_v1_16_R3(pathPoint);
    }

    @Override
    public @NotNull MobNavigator overrideNavigatorFor(Mob mob) throws NoSuchFieldException, IllegalAccessException {
        MobNavigator_v1_16_R3 customNavigator = new MobNavigator_v1_16_R3(((CraftMob)mob).getHandle(), ((CraftWorld)mob.getWorld()).getHandle());

        Field navigator = EntityInsentient.class.getDeclaredField("navigation");
        navigator.setAccessible(true);

        EntityInsentient craftMob = ((CraftMob)mob).getHandle();
        Navigation navigation = (Navigation) navigator.get(craftMob);

        if(!(navigation instanceof MobNavigator_v1_16_R3)) {
            navigator.set(((CraftMob)mob).getHandle(), customNavigator);
            return customNavigator;
        }
        else {
            return (MobNavigator_v1_16_R3)navigation;
        }
    }
}
