package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.game.data.map.RoomData;

public class NewRoomForm extends CommandForm<MapSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("room"),
            new Parameter("addbounds"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    public NewRoomForm() {
        super("Creates a new room.", Permissions.OPERATOR, parameters);
    }
    @Override
    public CommandValidator<MapSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_MAP_SELECTION;
    }

    @Override
    public String execute(Context context, Object[] arguments, MapSelectionData data) {
        String name = (String)arguments[2];

        for(RoomData room : data.getMap().getRooms()) {
            if(room.getName().equals(name)) {
                room.getBounds().addBounds(data.getSelection());
                data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
                return "Added new bounds to room.";
            }
        }

        RoomData newData = new RoomData(name);
        newData.getBounds().addBounds(data.getSelection());
        data.getMap().getRooms().add(newData);
        data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
        return "Created new room.";
    }
}
