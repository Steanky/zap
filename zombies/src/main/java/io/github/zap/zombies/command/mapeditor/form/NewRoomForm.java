package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class NewRoomForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("room"),
            new Parameter("bounds"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    public NewRoomForm() {
        super("Creates a new room.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator validator = new CommandValidator((context, form, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().getContext((Player)context.getSender());
        BoundingBox selection = editorContext.getSelection();
        MapData map = editorContext.getMap();

        if(!map.getMapBounds().contains(selection)) {
            return ValidationResult.of(false, "Room bounds must be entirely contained within the map!");
        }

        if(!map.checkOverlap((String)arguments[1], selection)) {
            return ValidationResult.of(false, "Rooms cannot overlap other rooms!");
        }

        return ValidationResult.of(true, null);
    }, new CommandValidator(MapeditorValidators.HAS_ACTIVE_MAP.getStep(), MapeditorValidators.HAS_SELECTION));

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        EditorContext editorContext = Zombies.getInstance().getContextManager().getContext((Player)context.getSender());
        BoundingBox selection = editorContext.getSelection();
        MapData map = editorContext.getMap();
        String name = (String)arguments[2];

        for(RoomData room : map.getRooms()) {
            if(room.getName().equals(name)) {
                room.getBounds().addBounds(selection.clone());
                editorContext.getRenderable(EditorContext.Renderables.ROOMS).update();
                return "Added new bounds to room.";
            }
        }

        RoomData newData = new RoomData(name);
        newData.getBounds().addBounds(selection);
        map.getRooms().add(newData);
        editorContext.getRenderable(EditorContext.Renderables.ROOMS).update();
        return "Created new room.";
    }
}
