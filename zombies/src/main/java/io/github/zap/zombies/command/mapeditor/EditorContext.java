package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.*;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable, RenderableProvider {
    private static final String BOUNDS_COMPONENT_NAME = "bounds";

    private final ParticleRenderer renderer;

    private final Player player;
    private final MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private Renderable boundsRenderable;

    private Renderable contextRenderable;

    public EditorContext(Player player, MapData editingMap) {
        this.player = player;
        this.editingMap = editingMap;

        renderer = new ParticleRenderer(player.getWorld(), player, 10);
        renderer.addRenderable(this);
    }

    public void addContextRenderable(RenderableProvider provider) {
        if(contextRenderable != null) {
            renderer.removeRenderable(contextRenderable.getName());
        }

        renderer.addRenderable(provider);
    }

    public void removeContextRenderable() {
        if(contextRenderable != null) {
            renderer.removeRenderable(contextRenderable.getName());
            contextRenderable = null;
        }
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

        RenderComponent component = getRenderable().getComponent(BOUNDS_COMPONENT_NAME);

        if(component != null) {
            component.updateVectors();
        }
    }

    public Renderable getRenderable() {
        if(boundsRenderable == null) {
            boundsRenderable = new Renderable("selection");

            boundsRenderable.addComponent(new CachingRenderComponent(BOUNDS_COMPONENT_NAME,
                    new ParticleSettings(Particle.CRIT, 1)) {
                @Override
                public void updateVectors() {
                    cache = VectorUtils.interpolateBounds(BoundingBox.of(firstClicked, secondClicked), 2);
                }
            });
        }

        return boundsRenderable;
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}