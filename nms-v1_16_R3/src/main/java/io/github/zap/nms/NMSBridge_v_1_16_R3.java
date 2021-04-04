package io.github.zap.nms;

import org.jetbrains.annotations.NotNull;

public class NMSBridge_v_1_16_R3 implements NMSBridge {
    private static final String VERSION = "1_16_R3";
    private static final EntityBridge_v_1_16_R3 ENTITY_BRIDGE = new EntityBridge_v_1_16_R3();
    private static final WorldBridge_v1_16_R3 WORLD_BRIDGE = new WorldBridge_v1_16_R3();

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge_v_1_16_R3 entityBridge() {
        return ENTITY_BRIDGE;
    }

    @Override
    public @NotNull WorldBridge_v1_16_R3 worldBridge() {
        return WORLD_BRIDGE;
    }
}
