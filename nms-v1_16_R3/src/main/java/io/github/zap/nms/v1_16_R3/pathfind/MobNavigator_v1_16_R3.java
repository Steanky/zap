package io.github.zap.nms.v1_16_R3.pathfind;

import io.github.zap.nms.common.pathfind.MobNavigator;
import io.github.zap.nms.common.pathfind.PathEntityWrapper;
import net.minecraft.server.v1_16_R3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class MobNavigator_v1_16_R3 extends Navigation implements MobNavigator {
    public MobNavigator_v1_16_R3(EntityInsentient entityinsentient, World world) {
        super(entityinsentient, world);
    }

    @Override
    public void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper) {
        super.a(((PathEntityWrapper_v1_16_R3)pathEntityWrapper).pathEntity(), 1);
    }

    @Override
    public PathEntity a(BlockPosition blockposition, int i) {
        return c;
    }

    @Override
    public PathEntity a(Entity entity, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(Stream<BlockPosition> stream, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(Set<BlockPosition> set, int i) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity a(BlockPosition blockposition, Entity target, int i) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, int i, boolean flag, int j) {
        return c;
    }

    @Nullable
    @Override
    protected PathEntity a(Set<BlockPosition> set, Entity target, int i, boolean flag, int j) {
        return c;
    }

    @Nullable
    @Override
    public PathEntity getPathEntity() {
        return c;
    }

    @Nullable
    @Override
    public PathEntity k() {
        return c;
    }
}
