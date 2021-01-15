package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.BasicRenderComponent;
import io.github.zap.arenaapi.particle.ParticleRenderer;
import io.github.zap.arenaapi.particle.ParticleSettings;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private static final String BOUNDS_RENDER_NAME = "bounds";
    private static final ParticleSettings PARTICLE_SETTINGS = new ParticleSettings(Particle.CRIT, 1);

    private final ParticleRenderer renderer;

    private final Player player;
    private final MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private Renderable renderTarget;

    public EditorContext(Player player, MapData editingMap) {
        this.renderer = new ParticleRenderer(Bukkit.getWorld(editingMap.getWorldName()), 10, player);
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

        //update or create vectors
        renderer.computeIfAbsent(BOUNDS_RENDER_NAME, mapper -> new BasicRenderComponent(BOUNDS_RENDER_NAME,
                PARTICLE_SETTINGS, this::boundsUpdate)).updateFragments();
    }

    private Vector[] boundsUpdate() {
        return VectorUtils.interpolateBounds(secondClicked == null ? BoundingBox.of(firstClicked, firstClicked) :
                BoundingBox.of(firstClicked, secondClicked), 2);
    }

    /**
     * Called in order to recalculate the vectors for the current render target.
     */
    public void redraw() {
        renderer.removeAllMatching((renderer) -> !renderer.name().equals(BOUNDS_RENDER_NAME));
        renderer.addComponents(renderTarget.getRenderComponents());
    }

    public void setRenderTarget(Renderable target) {
        renderTarget = target;
        redraw();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}