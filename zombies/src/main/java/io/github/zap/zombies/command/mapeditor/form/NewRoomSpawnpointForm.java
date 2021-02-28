package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.SpawnpointData;

public class NewRoomSpawnpointForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawn"),
            new Parameter("create"),
            new Parameter(Regexes.OBJECT_NAME, "[spawn-rule-name]")
    };

    private static final CommandValidator<RoomSelectionData, RoomSelectionData> validator =
            new CommandValidator<>((context, objects, roomSelectionData) -> {
        //noinspection SuspiciousMethodCalls
        if(!roomSelectionData.getMap().getSpawnRules().containsKey(objects[2])) {
            return ValidationResult.of(false, "A spawnrule with that name does not exist!", null);
        }

        return ValidationResult.of(true, null, roomSelectionData);
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewRoomSpawnpointForm() {
        super("Creates a new spawnpoint in a room.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomSelectionData data) {
        data.getRoom().getSpawnpoints().add(new SpawnpointData(data.getContext().getTarget(), null, null,
                (String)arguments[2]));
        data.getContext().updateRenderable(EditorContext.Renderables.SPAWNPOINTS);
        return "Added spawnpoint.";
    }
}
