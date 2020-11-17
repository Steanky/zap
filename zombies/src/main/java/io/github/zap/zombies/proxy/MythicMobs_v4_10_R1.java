package io.github.zap.zombies.proxy;

import io.lumine.xikage.mythicmobs.MythicMobs;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MythicMobs_v4_10_R1 implements MythicProxy {
    private final MythicMobs mythicPlugin;

    @Override
    public MythicMobs getMythicPlugin() {
        return mythicPlugin;
    }

    @Override
    public void injectCustomMechanics() {

    }
}
