package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;

public class EditMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("^(map)$", "map"),
            new Parameter("^(edit)$", "edit"),
            new Parameter("^(\\w+)$", "[map_name]")
    };

    private static final CommandValidator validator;

    static {
        validator = new CommandValidator((context, arguments) -> {
            Player sender = (Player)context.getSender();

            if(Zombies.getInstance().getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
                return new ImmutablePair<>(false, "You are already editing a map.");
            }

            if(!Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
                return new ImmutablePair<>(false, "A map with that name doesn't exist.");
            }

            return new ImmutablePair<>(true, null);
        });

        validator.chain(Validators.PLAYER_EXECUTOR);
    }

    public EditMapForm() {
        super("Edit an existing Zombies map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        String name = (String)arguments[2];
        Player player = (Player)context.getSender();
        Zombies.getInstance().getContextManager().getContextMap().put(player.getUniqueId(), new EditorContext(player,
                Zombies.getInstance().getArenaManager().getMaps().get(name)));

        return String.format("Editing map '%s'.", name);
    }
}
