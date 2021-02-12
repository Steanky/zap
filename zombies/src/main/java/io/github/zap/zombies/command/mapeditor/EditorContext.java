package io.github.zap.zombies.command.mapeditor;

import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.*;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private static final Shader SELECTION_SHADER = new SolidShader(Particle.FLAME, 1, null);

    private class SelectionRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SELECTION_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            return firstClicked == null ? VectorProvider.EMPTY : new Cube(BoundingBox.of(firstClicked,
                    secondClicked == null ? firstClicked : secondClicked), 1);
        }
    }

    private final SelectionRenderable boundsRenderable = new SelectionRenderable();

    private final Player player;

    @Setter
    private MapData map;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private final Renderer renderer;

    public EditorContext(Player player) {
        this.player = player;
        renderer = new SimpleRenderer(player.getWorld(), 0, 5);
        renderer.add(boundsRenderable);
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

    public BoundingBox getSelectedBounds() {
        if(firstClicked != null && secondClicked != null) {
            return BoundingBox.of(firstClicked, secondClicked);
        }
        else if(firstClicked != null) {
            return BoundingBox.of(firstClicked, firstClicked);
        }

        return null;
    }

    @Override
    public void dispose() {
        renderer.stop();
    }
}