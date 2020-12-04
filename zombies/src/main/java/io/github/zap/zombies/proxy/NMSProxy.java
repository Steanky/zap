package io.github.zap.zombies.proxy;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface NMSProxy {

    AtomicInteger getEntityCount();

    UUID randomUUID();

}
