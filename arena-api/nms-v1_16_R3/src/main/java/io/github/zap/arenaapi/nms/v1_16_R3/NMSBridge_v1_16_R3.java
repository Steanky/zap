package io.github.zap.arenaapi.nms.v1_16_R3;

import io.github.zap.arenaapi.nms.common.NMSBridge;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.itemstack.ItemStackBridge;
import io.github.zap.arenaapi.nms.common.player.PlayerBridge;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.nms.v1_16_R3.entity.EntityBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.itemstack.ItemStackBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.player.PlayerBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.world.WorldBridge_v1_16_R3;
import org.jetbrains.annotations.NotNull;

public class NMSBridge_v1_16_R3 implements NMSBridge {
    public static final NMSBridge_v1_16_R3 INSTANCE = new NMSBridge_v1_16_R3();
    private static final String VERSION = "v1_16_R3";

    private NMSBridge_v1_16_R3() {}

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return EntityBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull ItemStackBridge itemStackBridge() {
        return ItemStackBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull PlayerBridge playerBridge() {
        return PlayerBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull WorldBridge worldBridge() {
        return WorldBridge_v1_16_R3.INSTANCE;
    }
}
