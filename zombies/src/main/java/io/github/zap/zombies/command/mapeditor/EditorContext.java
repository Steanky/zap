package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
public class EditorContext implements Disposable {
    private final Player player;

    private MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    public EditorContext(Player player) {
        this.player = player;
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

    }
}