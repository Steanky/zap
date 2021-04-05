package io.github.zap.nms;

import io.github.zap.nms.entity.EntityBridge;
import io.github.zap.nms.world.WorldBridge;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

public class NMSBridge_v1_16_R3 implements NMSBridge {
    public static final NMSBridge_v1_16_R3 INSTANCE = new NMSBridge_v1_16_R3();

    private static final String VERSION = "v1_16_R3";
    private static final EntityBridge ENTITY_BRIDGE = new EntityBridge_v1_16_R3();
    private static final WorldBridge WORLD_BRIDGE = new WorldBridge_v1_16_R3();

    private NMSBridge_v1_16_R3() {}

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return ENTITY_BRIDGE;
    }

    @Override
    public @NotNull WorldBridge worldBridge() {
        return WORLD_BRIDGE;
    }
}
