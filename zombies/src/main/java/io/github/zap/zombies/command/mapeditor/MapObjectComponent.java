package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.particle.ParticleSettings;
import io.github.zap.arenaapi.particle.RenderComponent;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class MapObjectComponent implements RenderComponent {
    @Override
    public Vector[] getFragments() {
        return new Vector[0];
    }

    @Override
    public ParticleSettings particleData() {
        return null;
    }

    @Override
    public List<Player> renderTo() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }
}
