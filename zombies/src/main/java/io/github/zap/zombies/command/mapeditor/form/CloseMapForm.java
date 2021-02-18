package io.github.zap.zombies.command.mapeditor.form;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;

public class CloseMapForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("close"),
            new Parameter(Regexes.BOOLEAN, "[should-delete]", "false",
                    Converters.BOOLEAN_CONVERTER, Lists.newArrayList("true", "false"))
    };

    public CloseMapForm() {
        super("Closes an existing map, optionally discarding it.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public String execute(Context context, Object[] arguments, MapContextData data) {
        data.getContext().setMap(null);

        if((boolean)arguments[2]) {
            Zombies.getInstance().getArenaManager().removeMap(data.getMap().getName());
            return "Closed and deleted the current map.";
        }

        return "Closed the current map.";
    }
}
