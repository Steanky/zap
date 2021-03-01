package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class GuardianBeam extends BasicBeam {

    public GuardianBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level) {
        super(mapData, zombiesPlayer, root, level);
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            ZombiesPlayer zombiesPlayer = getZombiesPlayer();
            Player player = zombiesPlayer.getPlayer();

            if (determineIfHeadshot(rayTraceResult, mob)) {
                mob.setHealth(mob.getHealth() - getDamage());
                zombiesPlayer.addCoins(getGoldPerHeadshot());
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
            } else {
                mob.damage(getDamage());
                zombiesPlayer.addCoins(getGoldPerShot());
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1.5F, 1.0F);
            }

            mob.setVelocity(mob.getVelocity().add(getDirectionVector().clone().multiply(getKnockbackFactor())));

            if (mob.getHealth() <= 0) {
                getZombiesPlayer().incrementKills();
            }
        }
    }
}
