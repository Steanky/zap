package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import org.bukkit.entity.Player;

public class EditMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("edit"),
            new Parameter("^(\\w+)$", "[map_name]")
    };

    public EditMapForm() {
        super("Edit an existing Zombies map.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator validator;

    static {
        validator = MapeditorValidators.mapExistsValidator(2, MapeditorValidators.NO_ACTIVE_MAP);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        String name = (String)arguments[2];
        Player player = (Player)context.getSender();

        //TODO: set appropriate value in EditorSession

        return String.format("Now editing map '%s'.", name);
    }
}
