package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.*;
import io.github.zap.zombies.game.data.map.MapData;
import io.lumine.xikage.mythicmobs.utils.particles.ParticleData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class EditorContext implements Disposable {
    private static final Shader SELECTION_SHADER = new SolidShader(Particle.REDSTONE, 1, new Particle.DustOptions(Color.BLUE, 1));

    private final SelectionRenderable boundsRenderable = new SelectionRenderable();

    private class SelectionRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SELECTION_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            return firstClicked == null ? VectorProvider.EMPTY : new Cube(BoundingBox.of(firstClicked,
                    secondClicked == null ? firstClicked : secondClicked), 2);
        }
    }

    @Getter
    private final Player player;

    @Getter
    @Setter
    private MapData map;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private final Renderer renderer;

    @Getter
    private Renderable currentRenderable;

    public EditorContext(Player player) {
        this.player = player;

        renderer = new SimpleRenderer(player.getWorld(), 0, 5);
        renderer.add(boundsRenderable);
        renderer.start();
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

        boundsRenderable.update();
    }

    public BoundingBox getSelection() {
        if(firstClicked != null && secondClicked != null) {
            return BoundingBox.of(firstClicked, secondClicked);
        }
        else if(firstClicked != null) {
            return BoundingBox.of(firstClicked, firstClicked);
        }

        return null;
    }

    public void setCurrentRenderable(Renderable renderable) {
        this.currentRenderable = renderable;
        renderer.set(1, renderable);
    }

    @Override
    public void dispose() {
        renderer.stop();
    }
}