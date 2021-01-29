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
    private final ParticleRenderer renderer;

    private final Player player;

    private MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private Renderable boundsRenderable;
    private RenderComponent boundsComponent;

    private Renderable contextRenderable;

    public EditorContext(Player player) {
        this.player = player;

        renderer = new ParticleRenderer(player.getWorld(), player, 10);
        renderer.addRenderable(this);
        renderer.start();
    }

    public Vector getFirstClicked() {
        return firstClicked;
    }

    public Vector getSecondClicked() {
        if(secondClicked == null) {
            return firstClicked;
        }

        return secondClicked;
    }

    public void setEditingMap(MapData data) {
        if(data != editingMap) {
            editingMap = data;

            if(data == null) {
                renderer.removeRenderable(contextRenderable.getName());
                contextRenderable = null;
            }
            else {
                contextRenderable = data.getRenderable();
            }
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

        if(boundsComponent != null) {
            boundsComponent.updateVectors();
        }
    }

    public BoundingBox getSelectedBounds() {
        if(firstClicked != null && secondClicked != null) {
            return BoundingBox.of(firstClicked, secondClicked);
        }
        else if(firstClicked != null) {
            return BoundingBox.of(firstClicked, firstClicked);
        }

        return null;
    }

    public Renderable getRenderable() {
        if(boundsRenderable == null) {
            boundsRenderable = new Renderable("selection");

            boundsComponent = new CachingRenderComponent("bounds", new ParticleSettings(Particle.CRIT, 1)) {
                @Override
                public void updateVectors() {
                    BoundingBox renderBounds = getSelectedBounds();

                    if(renderBounds != null) {
                        cache = VectorUtils.interpolateBounds(renderBounds, 2);
                    }
                }
            };

            boundsRenderable.addComponent(boundsComponent);
        }

        return boundsRenderable;
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}