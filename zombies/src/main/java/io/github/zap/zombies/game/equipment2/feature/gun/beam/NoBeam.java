package io.github.zap.zombies.game.equipment2.feature.gun.beam;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class NoBeam implements Beam {

    @Override
    public void send(@NotNull World world, @NotNull Vector from, @NotNull Vector to, @NotNull Runnable onceHit) {
        onceHit.run();
    }

}
