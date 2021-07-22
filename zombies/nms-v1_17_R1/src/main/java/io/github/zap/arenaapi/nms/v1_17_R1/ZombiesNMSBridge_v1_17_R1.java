package io.github.zap.arenaapi.nms.v1_17_R1;

import io.github.zap.arenaapi.nms.v1_17_R1.entity.EntityBridge_v1_17_R1;
import io.github.zap.zombies.nms.common.ZombiesNMSBridge;
import io.github.zap.zombies.nms.common.entity.EntityBridge;
import org.jetbrains.annotations.NotNull;

public class ZombiesNMSBridge_v1_17_R1 implements ZombiesNMSBridge {

    public static final ZombiesNMSBridge_v1_17_R1 INSTANCE = new ZombiesNMSBridge_v1_17_R1();
    private static final String VERSION = "v1_17_R1";

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return EntityBridge_v1_17_R1.INSTANCE;
    }

}
