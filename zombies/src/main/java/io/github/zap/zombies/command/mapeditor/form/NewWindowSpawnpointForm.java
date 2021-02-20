package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.SpawnpointData;

public class NewWindowSpawnpointForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("roomspawn"),
            new Parameter("create"),
            new Parameter(Regexes.NON_NEGATIVE_NUMBER, "[window-index]")
    };

    private static final CommandValidator<RoomSelectionData, RoomSelectionData> validator = new CommandValidator<>((context, arguments, previousData) -> {
        if(previousData.getSelection().getVolume() != 1) {
            return ValidationResult.of(false, "You must select a single block!", null);
        }

        return ValidationResult.of(true, null, previousData);
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewWindowSpawnpointForm() {
        super("Creates a new spawnpoint in a window.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomSelectionData data) {
        data.getRoom().getSpawnpoints().add(new SpawnpointData(data.getSelection().getMin(), null, null,
                (String)arguments[2]));
        return "Added spawnpoint.";
    }
}
