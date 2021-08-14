package io.github.zap.zombies.game.equipment2.feature.gun.targeter;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Triple;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class NearestTargetSelector implements TargetSelector {

    private final boolean reuse;

    private final boolean headshots;

    public NearestTargetSelector(boolean reuse, boolean headshots) {
        this.reuse = reuse;
        this.headshots = headshots;
    }

    @Override
    public @NotNull List<TargetSelection> selectTargets(@NotNull MapData map, @NotNull World world,
                                                        @NotNull Set<Mob> candidates, @NotNull Set<Mob> used,
                                                        @NotNull Vector root, @NotNull Vector previousDirection,
                                                        @NotNull List<Boolean> headshotHistory, int maxRange,
                                                        int limit) {
        List<TargetSelection> selection = new ArrayList<>();

        List<Triple<RayTraceResult, Vector, Mob>> rayTraces = new ArrayList<>(candidates.size());
        Map<Mob, Double> distances = new HashMap<>();
        if (reuse) {
            for (Mob mob : candidates) {
                Vector endpoint = (headshots) ? mob.getEyeLocation().toVector() : mob.getBoundingBox().getCenter();
                Vector direction = endpoint.subtract(root).normalize();
                double range = root.distanceSquared(endpoint);
                RayTraceResult rayTrace = mob.getBoundingBox().rayTrace(root, direction, range);
                if (rayTrace != null) {
                    distances.put(mob, rayTrace.getHitPosition().distanceSquared(root));
                    rayTraces.add(Triple.of(rayTrace, direction, mob));
                }
            }
        }
        else {
            for (Mob mob : candidates) {
                if (!used.contains(mob)) {
                    Vector endpoint = (headshots) ? mob.getEyeLocation().toVector() : mob.getBoundingBox().getCenter();
                    Vector direction = endpoint.subtract(root).normalize();
                    double range = root.distanceSquared(endpoint);
                    RayTraceResult rayTrace = mob.getBoundingBox().rayTrace(root, direction, range);
                    if (rayTrace != null) {
                        distances.put(mob, rayTrace.getHitPosition().distanceSquared(root));
                        rayTraces.add(Triple.of(rayTrace, direction, mob));
                    }
                }
            }
        }

        rayTraces.sort(Comparator.comparingDouble(o -> distances.get(o.getRight())));
        int bound = Math.min(rayTraces.size(), limit);
        for (int i = 0; i < bound; i++) {
            Triple<RayTraceResult, Vector, Mob> rayTrace = rayTraces.get(i);
            selection.add(new TargetSelection(rayTrace.getRight(), rayTrace.getMiddle(),
                    rayTrace.getLeft().getHitPosition(), true, headshots));
            used.add(rayTrace.getRight());
        }

        return selection;
    }

}
