package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

/**
 * Gun beam which freezes zombies
 */
public class GuardianBeam extends BasicBeam {

    private final int freezeTime;

    public GuardianBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, GuardianGunLevel level) {
        super(mapData, zombiesPlayer, root, level);
        this.freezeTime = level.getFreezeTime();
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            ActiveMob activeMob = getBukkitAPIHelper().getMythicMobInstance(mob);

            if (activeMob != null) {
                ZombiesPlayer zombiesPlayer = getZombiesPlayer();
                Player player = zombiesPlayer.getPlayer();

                boolean isCritical = determineIfHeadshot(rayTraceResult, mob);
                mob.playEffect(EntityEffect.HURT);
                inflictDamage(mob, getDamage(), isCritical);
                zombiesPlayer.addCoins(isCritical ? getGoldPerHeadshot() : getGoldPerShot());
                player.playSound(Sound.sound(
                        Key.key("minecraft:entity.arrow.hit.player"),
                        Sound.Source.MASTER,
                        1.0F,
                        isCritical ? 2.0F : 1.5F
                ));

                AbstractEntity abstractEntity = activeMob.getEntity();
                abstractEntity.setMovementSpeed(0.0D);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        double speed = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getDefaultValue();
                        abstractEntity.setMovementSpeed(speed);
                    }
                }.runTaskLater(Zombies.getInstance(), freezeTime);
                mob.setVelocity(mob.getVelocity().add(getDirectionVector().clone().multiply(getKnockbackFactor())));

                if (mob.getHealth() <= 0) {
                    getZombiesPlayer().incrementKills();
                }
            }
        }
    }
}
