package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class EditorContextData {
    private final Player player;
    private final EditorContext context;

    public EditorContextData(Player player, EditorContext context) {
        this.player = player;
        this.context = context;
    }
}
