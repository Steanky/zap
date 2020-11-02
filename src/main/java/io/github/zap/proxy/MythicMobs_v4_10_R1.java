package io.github.zap.proxy;

import io.lumine.xikage.mythicmobs.MythicMobs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MythicMobs_v4_10_R1 implements MythicProxy {
    @Getter
    private final MythicMobs mythicMobs;

    @Override
    public void injectCustomMechanics() {

    }
}
