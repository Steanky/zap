package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

public class NewMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[name]")
    };

    private static final CommandValidator validator = MapeditorValidators.mapAbsentValidator(2,
            MapeditorValidators.NO_ACTIVE_MAP);

    public NewMapForm() {
        super("Creates a new session with the mapeditor.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Zombies zombies = Zombies.getInstance();
        Player player = (Player)context.getSender();

        MapData map = new MapData((String)arguments[2], player.getWorld().getName());
        EditorContext editorContext = zombies.getContextManager().fetchContext(player);

        editorContext.setEditingMap(map);

        return String.format("Created new map '%s'.", map.getName());
    }
}
