package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

public class NewMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter("^(\\w+)$", "[map_name]")
    };

    private static final CommandValidator validator;

    static {
        validator = MapeditorValidators.mapAbsentValidator(2,
                new CommandValidator(MapeditorValidators.BOUNDS_REQUIRED.getStep(), MapeditorValidators.NO_ACTIVE_MAP));
    }

    public NewMapForm() {
        super("Create a new Zombies map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        String name = (String)arguments[2];
        Player player = (Player)context.getSender();
        String worldName = player.getWorld().getName();

        MapData newMap = new MapData(name, worldName);

        Zombies zombies = Zombies.getInstance();
        zombies.getContextManager().fetchContext(player).setEditingMap(newMap);
        zombies.getArenaManager().addMap(newMap);

        return String.format("Now editing new map '%s' for world '%s'.", name, worldName);
    }
}