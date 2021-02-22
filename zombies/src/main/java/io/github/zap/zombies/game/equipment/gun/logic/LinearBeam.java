package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Sends lines of particles from guns
 */
@Getter
public class LinearBeam extends BasicBeam { // TODO: figuring out particle data

    public final static int DEFAULT_PARTICLE_COUNT = 4; // TODO: check

    private final Particle particle;
    private final int particleCount;


    public LinearBeam(MapData mapData, Location root, Particle particle, LinearGunLevel level, int particleCount) {
        super(mapData, root, level);

        this.particle = particle;
        this.particleCount = particleCount;
    }

    public LinearBeam(MapData mapData, Location root, Particle particle, LinearGunLevel level) {
        this(mapData, root, particle, level, DEFAULT_PARTICLE_COUNT);
    }

    /**
     * Sends the bullet
     */
    public void send() {
        super.send();
        spawnParticles();
    }

    /**
     * Spawns the bullet's particles in a line
     */
    private void spawnParticles() {
        World world = getWorld();
        Location rootLocation = getRoot().toLocation(world);

        for (int i = 0; i < particleCount; i++) {
            world.spawnParticle(
                    particle,
                    rootLocation,
                    0,
                    0,
                    0,
                    0
            );
            rootLocation.add(getDirectionVector());
        }
    }

}
