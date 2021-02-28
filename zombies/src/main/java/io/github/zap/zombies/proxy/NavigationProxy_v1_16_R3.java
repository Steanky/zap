package io.github.zap.zombies.proxy;

import com.google.common.collect.ImmutableSet;
import io.github.zap.arenaapi.util.ListUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class NavigationProxy_v1_16_R3 implements NavigationProxy {
    private final NavigationAbstract navigator;
    private static Method pathfind = null;

    public NavigationProxy_v1_16_R3(NavigationAbstract navigator) {
        this.navigator = navigator;

        if(pathfind == null) {
            try {
                pathfind = NavigationAbstract.class.getDeclaredMethod("a", Set.class, Entity.class, int.class,
                        boolean.class, int.class);
                pathfind.setAccessible(true); //screw you Mojang, i'm going to call this method and you can't stop me
            } catch (NoSuchMethodException e) {
                Zombies.severe("NoSuchMethodException when attempting to reflect NavigationAbstract pathfinding!");
            }
        }
    }

    public ZombiesPlayer findClosest(ZombiesArena arena, Predicate<ZombiesPlayer> filter, int range) {
        Pair<Float, ZombiesPlayer> bestCandidate = ImmutablePair.of(Float.MAX_VALUE, null);

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            if(filter.test(player)) {
                Player bukkitPlayer = player.getPlayer();
                Location location = bukkitPlayer.getLocation();
                PathEntity path = navigator.a(new BlockPosition(location.getX(), location.getY(), location.getZ()), 0);

                if(path != null) {
                    PathPoint finalPoint = path.getFinalPoint();
                    if(finalPoint != null) {
                        if(finalPoint.e < bestCandidate.getLeft()) {
                            bestCandidate = ImmutablePair.of(finalPoint.e, player);
                        }
                    }
                }
            }
        }

        return bestCandidate.getRight();
    }
}
