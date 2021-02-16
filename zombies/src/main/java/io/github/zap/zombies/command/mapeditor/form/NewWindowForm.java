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
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class NewWindowForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("window"),
            new Parameter("add")
    };

    public NewWindowForm() {
        super("Creates a new window.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator validator = new CommandValidator((context, form, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().getContext((Player)context.getSender());
        BoundingBox selection = editorContext.getSelection();
        MapData map = editorContext.getMap();

        for(RoomData room : map.getRooms()) {
            if(room.getBounds().contains(selection)) {
                arguments[0] = room; //dirty but effective hack: we don't have to lookup the room again
                //note to self: add an easy/safer way to send data from the validator to the executor
                return ValidationResult.of(true, null);
            }
        }

        return ValidationResult.of(false, "Can't place a window outside of a room!");
    }, new CommandValidator(MapeditorValidators.HAS_ACTIVE_MAP.getStep(), MapeditorValidators.HAS_SELECTION));

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        EditorContext editorContext = Zombies.getInstance().getContextManager().getContext((Player)context.getSender());
        BoundingBox selection = editorContext.getSelection();
        Player sender = (Player)context.getSender();
        RoomData target = (RoomData)arguments[0];
        target.getWindows().add(new WindowData(sender.getWorld(), selection));
        return "Created new room.";
    }
}
