package io.github.zap.zombies.command.mapeditor.form;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import org.bukkit.util.BoundingBox;

public class RemoveRoomForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("room"),
            new Parameter("remove"),
            new Parameter(Regexes.BOOLEAN, "[delete-all]", "false",
                    Converters.BOOLEAN_CONVERTER, Lists.newArrayList("true", "false"))
    };

    public RemoveRoomForm() {
        super("Removes a room.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator<RoomSelectionData, MapSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
                BoundingBox selection = previousData.getSelection();

                for(RoomData room : previousData.getMap().getRooms()) {
                    if(room.getBounds().overlaps(selection)) {
                        return ValidationResult.of(true, null, new RoomSelectionData(previousData.getPlayer(),
                                previousData.getContext(), previousData.getSelection(), previousData.getMap(), room));
                    }
                }

                return ValidationResult.of(false, "No room or room bounds overlap that selection.", null);
            }, MapeditorValidators.HAS_MAP_SELECTION);

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomSelectionData data) {
        if((boolean)arguments[2]) {
            data.getMap().getRooms().remove(data.getRoom());
            data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
            data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);
            return "Removed room '" + data.getRoom().getName() + "'.";
        }

        MultiBoundingBox roomBounds = data.getRoom().getBounds();

        int j = 0;
        for(int i = roomBounds.size() - 1; i > -1; i--) {
            BoundingBox bounds = roomBounds.get(i);
            if(bounds.overlaps(data.getSelection())) {
                roomBounds.remove(i);
                j++;
            }
        }

        data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
        data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);

        return "Removed " + j + " bounds from room '" + data.getRoom().getName() + "'.";
    }
}
