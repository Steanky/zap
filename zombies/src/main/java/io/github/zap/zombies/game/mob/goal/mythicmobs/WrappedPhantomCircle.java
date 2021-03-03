package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.mob.goal.WrappedZombiesPathfinder;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.EntityPhantom;
import net.minecraft.server.v1_16_R3.PathfinderGoal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@MythicAIGoal(
        name = "unboundedPhantomCircle"
)
public class WrappedPhantomCircle extends MythicWrapper {
    public WrappedPhantomCircle(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
    }

    @Override
    public PathfinderGoal create() {
        EntityPhantom entityPhantom = (EntityPhantom)getHandle();

        Class<?>[] classes = EntityPhantom.class.getDeclaredClasses(); //ew
        for(Class<?> sample : classes) {
            String simpleName = sample.getSimpleName();
            if(simpleName.equals("c")) { //inner class is pathfinder for circling phantoms
                Constructor<?> constructor = sample.getDeclaredConstructors()[0];
                constructor.setAccessible(true); //screw you mojang

                try {
                    return new WrappedZombiesPathfinder(entity, (PathfinderGoal) constructor.newInstance(entityPhantom), getRetargetInterval());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
                    Zombies.warning("Reflection-related exception when initializing Phantom AI!");
                }
            }
        }

        return null;
    }

    @Override
    public boolean isValid() {
        return getHandle() instanceof EntityPhantom;
    }
}