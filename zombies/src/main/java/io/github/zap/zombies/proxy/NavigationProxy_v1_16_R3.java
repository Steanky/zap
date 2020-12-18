package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.util.ListUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NavigationAbstract;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class NavigationProxy_v1_16_R3 implements NavigationProxy {
    private final NavigationAbstract navigator;

    public ZombiesPlayer findClosest(ZombiesArena arena) {
        Map<BlockPosition, List<ZombiesPlayer>> positionMappings = new HashMap<>();

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            Player bukkitPlayer = player.getPlayer();
            Location location = bukkitPlayer.getLocation();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(),
                    location.getBlockZ());

            positionMappings.putIfAbsent(blockPosition, new ArrayList<>());
            positionMappings.get(blockPosition).add(player);
        }

        PathEntity path = navigator.a(positionMappings.keySet(), 1024);
        if(path != null) {
            PathPoint finalPoint = path.getFinalPoint();

            if(finalPoint != null) {
                BlockPosition targetPosition = finalPoint.a();
                List<ZombiesPlayer> players = positionMappings.get(targetPosition);

                if(players != null) {
                    return ListUtils.randomElement(players);
                }
                else {
                    Zombies.warning(String.format("Resulting List<ZombiesPlayer> for BlockPosition %s is null or " +
                            "empty.", targetPosition));
                }
            }
            else {
                Zombies.info("PathEntity#getFinalPoint() returned null, meaning the navigator was unable to find a" +
                        " valid path to any player.");
            }
        }
        else {
            Zombies.warning("NavigationAbstract#a(Set<BlockPosition>,int) returned null.");
        }

        return null;
    }
}
