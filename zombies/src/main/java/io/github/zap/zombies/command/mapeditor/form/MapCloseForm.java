package io.github.zap.zombies.command.mapeditor.form;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

public class MapCloseForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("close"),
            new Parameter("^((true)|(false))$", "[should-discard]", "false",
                    Converters.BOOLEAN_CONVERTER, Lists.newArrayList("true", "false"))
    };

    public MapCloseForm() {
        super("Closes an existing map, optionally discarding it.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Zombies zombies = Zombies.getInstance();
        EditorContext editorContext = zombies.getContextManager().fetchContext((Player)context.getSender());

        MapData map = editorContext.getMap();

        if((boolean)arguments[2]) {
            zombies.getArenaManager().removeMap(map.getName());
            return "Closed and deleted the current map.";
        }

        return "Closed the current map.";
    }
}
