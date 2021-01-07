package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.GlobalRenderComponent;
import io.github.zap.arenaapi.particle.ParticleRenderer;
import io.github.zap.arenaapi.particle.ParticleSettings;
import io.github.zap.arenaapi.particle.RenderComponent;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private static final String BOUNDS_RENDER_NAME = "bounds";
    private static final ParticleSettings PARTICLE_SETTINGS = new ParticleSettings(Particle.CRIT, 10);

    private final ParticleRenderer renderer;

    private final Player player;
    private final MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    public EditorContext(Player player, MapData editingMap) {
        this.renderer = new ParticleRenderer(Bukkit.getWorld(editingMap.getWorldName()), 10);
        this.player = player;
        this.editingMap = editingMap;
    }

    public void handleClicked(Block at) {
        if(firstClicked == null && secondClicked == null) {
            firstClicked = at.getLocation().toVector();
        }
        else if(firstClicked != null && secondClicked == null) {
            secondClicked = at.getLocation().toVector();
        }
        else if(firstClicked != null) {
            firstClicked = secondClicked;
            secondClicked = at.getLocation().toVector();
        }

        //update or create vectors
        renderer.computeIfAbsent(BOUNDS_RENDER_NAME, mapper -> new GlobalRenderComponent(BOUNDS_RENDER_NAME,
                PARTICLE_SETTINGS, Bukkit.getWorld(editingMap.getWorldName()))).updateFragments(
                        VectorUtils.particleAabb(BoundingBox.of(firstClicked, secondClicked), 2));
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
