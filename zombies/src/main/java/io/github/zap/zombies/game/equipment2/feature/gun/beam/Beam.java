package io.github.zap.zombies.game.equipment2.feature.gun.beam;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface Beam {

    void send(@NotNull World world, @NotNull Vector from, @NotNull Vector to, @NotNull Runnable onceHit);

}
