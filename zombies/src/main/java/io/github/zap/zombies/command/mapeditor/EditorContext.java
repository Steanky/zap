package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.Renderer;
import io.github.zap.arenaapi.particle.SimpleRenderer;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private final Player player;

    private final MapData map;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private final Renderer renderer;

    public EditorContext(Player player, MapData map) {
        this.player = player;
        this.map = map;
        renderer = new SimpleRenderer(player.getWorld(), 0, 5);
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