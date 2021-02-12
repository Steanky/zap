package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

public class NewMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[name]")
    };

    private static final CommandValidator validator = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
            return ValidationResult.of(false, "A map with that name already exists.");
        }

        return ValidationResult.of(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public NewMapForm() {
        super("Creates a new session with the mapeditor.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player)context.getSender();

        MapData map = new MapData((String)arguments[2], player.getWorld().getName());
        Zombies.getInstance().getContextManager().fetchOrCreate(player).setMap(new MapData((String)arguments[2],
                player.getWorld().getName()));

        return String.format("Created new map '%s'.", map.getName());
    }
}
