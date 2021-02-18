package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import org.bukkit.util.BoundingBox;

public class NewWindowForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("window"),
            new Parameter("add")
    };

    public NewWindowForm() {
        super("Creates a new window.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator<RoomSelectionData, MapSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        BoundingBox selection = previousData.getSelection();

        for(RoomData room : previousData.getMap().getRooms()) {
            if(room.getBounds().contains(selection)) {
                return ValidationResult.of(true, null, new RoomSelectionData(previousData.getPlayer(),
                        previousData.getContext(), previousData.getSelection(), previousData.getMap(), room));
            }
        }

        return ValidationResult.of(false, "Can't place a window outside of a room!", null);
    }, MapeditorValidators.HAS_MAP_SELECTION);

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomSelectionData data) {
        data.getRoom().getWindows().add(new WindowData(data.getPlayer().getWorld(), data.getSelection()));
        data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);
        return "Added window.";
    }
}
