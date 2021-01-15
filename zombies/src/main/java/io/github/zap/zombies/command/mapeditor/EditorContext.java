package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.ParticleSettings;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private static final String BOUNDS_RENDER_NAME = "bounds";
    private static final ParticleSettings BOUNDS_PARTICLE_SETTINGS = new ParticleSettings(Particle.CRIT, 1);

    private final Player player;
    private final MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    public EditorContext(Player player, MapData editingMap) {
        this.player = player;
        this.editingMap = editingMap;
    }

    public void handleClicked(Block at) {
        Vector clickedVector = at.getLocation().toVector();

        if(firstClicked == null && secondClicked == null) {
            firstClicked = clickedVector;
        }
        else if(firstClicked != null && secondClicked == null) {
            secondClicked = clickedVector;
        }
        else if(firstClicked != null) {
            firstClicked = secondClicked;
            secondClicked = clickedVector;
        }
    }

    @Override
    public void dispose() {

    }
}