package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

public class DeleteObjectForm extends CommandForm<MapSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("object"),
            new Parameter("remove")
    };

    public DeleteObjectForm() {
        super("Removes selected map objects.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_MAP_SELECTION;
    }

    @Override
    public String execute(Context context, Object[] arguments, MapSelectionData data) {
        int removedObjects = 0;

        MapData map = data.getMap();
        BoundingBox selection = data.getSelection();
        List<RoomData> rooms = map.getRooms();
        for(int roomIndex = rooms.size() - 1; roomIndex > -1; roomIndex--) {
            RoomData room = rooms.get(roomIndex);
            List<BoundingBox> roomBounds = room.getBounds().getList();

            if(room.getBounds().inside(selection)) {
                map.getRooms().remove(roomIndex);
                removedObjects += roomBounds.size() + room.getWindows().size() + room.getSpawnpoints().size() + 1;
                continue;
            }

            for(int boundsIndex = roomBounds.size() - 1; boundsIndex > -1; boundsIndex--) {
                BoundingBox bounds = roomBounds.get(boundsIndex);

                if(selection.contains(bounds)) {
                    roomBounds.remove(boundsIndex);
                    removedObjects++;
                }

                List<WindowData> windows = room.getWindows();
                for(int windowIndex = windows.size() - 1; windowIndex > -1; windowIndex--) {
                    WindowData window = windows.get(windowIndex);

                    if(selection.contains(window.getFaceBounds())) {
                        windows.remove(windowIndex);
                        removedObjects += window.getInteriorBounds().size() + 1;

                        List<SpawnpointData> spawnpoints = room.getSpawnpoints();
                        for(int spawnpointIndex = spawnpoints.size() - 1; spawnpointIndex > -1; spawnpointIndex--) {
                            SpawnpointData spawnpoint = spawnpoints.get(spawnpointIndex);

                            Vector windowFace = spawnpoint.getWindowFace();
                            for(int targetWindowIndex = windows.size() - 1; targetWindowIndex > -1; targetWindowIndex--) {
                                WindowData targetWindow = windows.get(targetWindowIndex);

                                if(targetWindow.getFaceBounds().contains(windowFace)) {
                                    spawnpoints.remove(spawnpointIndex);
                                    removedObjects++;
                                }
                            }
                        }

                        continue;
                    }

                    List<BoundingBox> interiorBounds = window.getInteriorBounds().getList();
                    for(int interiorBoundsIndex = interiorBounds.size() - 1; interiorBoundsIndex > -1; interiorBoundsIndex--) {
                        BoundingBox interiorBound = interiorBounds.get(interiorBoundsIndex);

                        if(selection.contains(interiorBound)) {
                            interiorBounds.remove(interiorBoundsIndex);
                            removedObjects++;
                        }
                    }
                }

                List<SpawnpointData> spawnpoints = room.getSpawnpoints();
                for(int spawnpointIndex = spawnpoints.size() - 1; spawnpointIndex > -1; spawnpointIndex--) {
                    SpawnpointData spawnpoint = spawnpoints.get(spawnpointIndex);

                    Vector spawn = spawnpoint.getSpawn();
                    Vector target = spawnpoint.getTarget();

                    if((spawn != null && selection.contains(spawn)) || (target != null && selection.contains(target))) {
                        spawnpoints.remove(spawnpointIndex);
                        removedObjects++;
                    }
                }
            }
        }

        if(removedObjects > 0) {
            data.getContext().updateAllRenderables();
        }

        return "Removed " + removedObjects + " objects.";
    }
}
