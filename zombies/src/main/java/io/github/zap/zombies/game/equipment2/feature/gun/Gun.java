package io.github.zap.zombies.game.equipment2.feature.gun;

import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.equipment2.feature.Feature;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class Gun implements Feature {

    private final Collection<Shot> shots;

    public Gun(@NotNull Collection<Shot> shots) {
        this.shots = shots;
    }

    public void shoot(@NotNull ZombiesPlayer player, @NotNull Set<Mob> candidates, @NotNull MapData map,
                      @NotNull World world) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            Location eyeLocation = bukkitPlayer.getEyeLocation();
            Set<Mob> used = new HashSet<>();

            for (Shot shot : shots) {
                shot.shoot(map, world, player, candidates, used, eyeLocation.toVector(), eyeLocation.getDirection(),
                        new ArrayList<>());
            }
        } else throw new IllegalArgumentException("Tried to shoot for a player that is not online!");
    }

    @Override
    public @Nullable ItemStack getVisual(@NotNull Equipment equipment) {
        return null; // TODO: implement
    }

}
