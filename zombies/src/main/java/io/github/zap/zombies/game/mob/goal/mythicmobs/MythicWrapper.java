package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.WrappedPathfindingGoal;
import io.lumine.xikage.mythicmobs.volatilecode.v1_16_R3.ai.PathfinderHolder;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.Entity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;

public abstract class MythicWrapper extends WrappedPathfindingGoal implements PathfinderHolder {
    @Getter
    private final Entity handle;

    @Getter
    private final ZombiesNMSProxy proxy;

    protected final int retargetTicks;

    public MythicWrapper(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        handle = ((CraftEntity) entity.getBukkitEntity()).getHandle();
        proxy = Zombies.getInstance().getNmsProxy();

        retargetTicks = mlc.getInteger("retargetTicks", 10);
    }
}
