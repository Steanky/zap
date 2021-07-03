package io.github.zap.nms.v1_16_R3.entity;

import io.github.zap.nms.common.entity.EntityBridge;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.nms.common.pathfind.PathPointWrapper;
import io.github.zap.nms.v1_16_R3.pathfind.PathEntityWrapper_v1_16_R3;
import io.github.zap.nms.v1_16_R3.pathfind.PathPointWrapper_v1_16_R3;
import io.github.zap.vector.VectorAccess;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                                                     @NotNull VectorAccess target, boolean reachesDestination) {
        List<PathPoint> points = new ArrayList<>();
        for(PathPointWrapper wrapper : pointWrappers) {
            PathPointWrapper_v1_16_R3 specific = (PathPointWrapper_v1_16_R3)wrapper;
            points.add(specific.pathPoint());
        }

        return new PathEntityWrapper_v1_16_R3(new PathEntity(points, new BlockPosition(target.blockX(), target.blockY(),
                target.blockZ()), reachesDestination));
    }

    @Override
    public @NotNull PathPointWrapper makePathPoint(@NotNull VectorAccess blockLocation) {
        PathPoint pathPoint = new PathPoint(blockLocation.blockX(), blockLocation.blockY(), blockLocation.blockZ());
        pathPoint.l = PathType.WALKABLE;
        return new PathPointWrapper_v1_16_R3(pathPoint);
    }
}
