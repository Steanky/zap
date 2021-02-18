package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class BoundsContextData extends EditorContextData {
    private final BoundingBox bounds;

    public BoundsContextData(Player player, EditorContext context, BoundingBox bounds) {
        super(player, context);
        this.bounds = bounds;
    }
}
