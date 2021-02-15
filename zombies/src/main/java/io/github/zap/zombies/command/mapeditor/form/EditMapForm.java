package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.Regexes;
import org.bukkit.entity.Player;

public class EditMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("edit"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    private static final CommandValidator validator = new CommandValidator((context, form, arguments) -> {
        if(!Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
            return ValidationResult.of(false, "That map does not exist!");
        }

        return ValidationResult.of(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public EditMapForm() {
        super("Edits an existing map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Zombies zombies = Zombies.getInstance();
        Player player = (Player)context.getSender();
        String mapName = (String)arguments[2];

        EditorContext editorContext = zombies.getContextManager().getContext(player);
        editorContext.setMap(zombies.getArenaManager().getMap(mapName));

        return String.format("Now editing map %s", mapName);
    }
}
