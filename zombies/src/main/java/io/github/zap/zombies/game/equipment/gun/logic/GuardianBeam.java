package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;

public class GuardianBeam extends BasicBeam {

    public GuardianBeam(MapData mapData, Location root, LinearGunLevel level) {
        super(mapData, root, level);
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            if (determineIfHeadshot(rayTraceResult, mob)) {
                mob.setHealth(mob.getHealth() - getDamage());
            } else {
                mob.damage(getDamage());
            }
            mob.setVelocity(mob.getVelocity().add(getDirectionVector().clone().multiply(getKnockbackFactor())));
        }
    }
}
