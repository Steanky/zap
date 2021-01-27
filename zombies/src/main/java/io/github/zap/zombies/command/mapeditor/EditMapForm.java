package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.completer.ArgumentCompleter;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Completers;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
    private static final ArgumentCompleter completer;

    static {
        validator = MapeditorValidators.mapExistsValidator(2, MapeditorValidators.NO_ACTIVE_MAP);

        //users can tab complete existing maps. fancy, unnecessary features
        completer = new ArgumentCompleter((context, form, args) -> {
            if(args.length == 3) {
                List<String> possibleMaps = new ArrayList<>();

                for(MapData map : Zombies.getInstance().getArenaManager().getMaps()) {
                    String name = map.getName();

                    if(name != null) {
                        String input = args[2];
                        if(name.startsWith(input)) {
                            possibleMaps.add(input);
                        }
                    }
                }

                return possibleMaps.size() == 0 ? null : possibleMaps;
            }

            return null;
        }, Completers.PARAMETER_COMPLETER);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public ArgumentCompleter getCompleter() {
        return completer;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        String name = (String)arguments[2];
        Player player = (Player)context.getSender();

        Zombies zombies = Zombies.getInstance();
        MapData target = zombies.getArenaManager().getMap(name);

        zombies.getContextManager().fetchContext(player).setEditingMap(target);

        return String.format("Now editing map '%s'.", name);
    }
}
