package io.github.zap.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.validator.CommandValidator;

public class MapCreationForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("(create)", "[create]"),
            new Parameter("(map)", "[create]")
    };

    public MapCreationForm() {
        super("Creates a new map.", Permissions.MAPEDITOR_PERMISSIONS, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return null;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        return null;
    }
}
