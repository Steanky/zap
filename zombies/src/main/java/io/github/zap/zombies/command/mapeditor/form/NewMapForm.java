package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
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
    }, MapeditorValidators.HAS_SELECTION);

    public NewMapForm() {
        super("Creates a new map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player)context.getSender();
        String mapName = (String)arguments[2];
        String worldName = player.getWorld().getName();

        MapData map = new MapData(mapName, worldName);
        Zombies.getInstance().getContextManager().getContext(player).setMap(map);

        return String.format("Created new map '%s' in world %s", mapName, worldName);
    }
}
