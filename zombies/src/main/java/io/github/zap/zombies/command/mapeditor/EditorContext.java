package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.particle.GlobalRenderComponent;
import io.github.zap.arenaapi.particle.ParticleRenderer;
import io.github.zap.arenaapi.particle.RenderComponent;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class EditorContext {
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
    }

    public void renderBounds() {

    }
}
