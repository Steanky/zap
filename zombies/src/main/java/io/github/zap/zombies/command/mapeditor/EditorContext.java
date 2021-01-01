package io.github.zap.zombies.command.mapeditor;

import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
@Getter
public class EditorContext {
    private final MapData editingMap;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

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
    }
}
