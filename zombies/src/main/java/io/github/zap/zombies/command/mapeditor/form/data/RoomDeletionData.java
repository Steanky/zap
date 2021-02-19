package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.List;

@Getter
public class RoomDeletionData extends MapSelectionData {
    private final List<Pair<RoomData, List<Integer>>> roomIndices;

    public RoomDeletionData(Player player, EditorContext context, BoundingBox bounds, MapData map,
                            List<Pair<RoomData, List<Integer>>> roomIndices) {
        super(player, context, bounds, map);
        this.roomIndices = roomIndices;
    }
}
