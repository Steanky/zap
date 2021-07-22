package io.github.zap.arenaapi.v1_17_R1;

import io.github.zap.arenaapi.nms.common.ArenaNMSBridge;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.itemstack.ItemStackBridge;
import io.github.zap.arenaapi.nms.common.player.PlayerBridge;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.v1_17_R1.entity.EntityBridge_v1_17_R1;
import io.github.zap.arenaapi.v1_17_R1.itemstack.ItemStackBridge_v1_17_R1;
import io.github.zap.arenaapi.v1_17_R1.player.PlayerBridge_v1_17_R1;
import io.github.zap.arenaapi.v1_17_R1.world.WorldBridge_v1_17_R1;
import org.jetbrains.annotations.NotNull;

public class ArenaNMSBridge_v1_17_R1 implements ArenaNMSBridge {
    public static final ArenaNMSBridge_v1_17_R1 INSTANCE = new ArenaNMSBridge_v1_17_R1();
    private static final String VERSION = "v1_17_R1";

    private ArenaNMSBridge_v1_17_R1() {}

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return EntityBridge_v1_17_R1.INSTANCE;
    }

    @Override
    public @NotNull ItemStackBridge itemStackBridge() {
        return ItemStackBridge_v1_17_R1.INSTANCE;
    }

    @Override
    public @NotNull PlayerBridge playerBridge() {
        return PlayerBridge_v1_17_R1.INSTANCE;
    }

    @Override
    public @NotNull WorldBridge worldBridge() {
        return WorldBridge_v1_17_R1.INSTANCE;
    }
}
