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
import org.bukkit.event.Listener;

public class NewMapForm extends CommandForm implements Listener {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("^(map)$", "map"),
            new Parameter("^(create)$", "create")
    };

    private static final CommandValidator validator = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        if(Zombies.getInstance().getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
            return new ImmutablePair<>(false, "You are already editing an existing map.");
        }

        return new ImmutablePair<>(true, null);
    });

    static {
        validator.chain(Validators.PLAYER_EXECUTOR);
    }

    public NewMapForm() {
        super("usage", Permissions.OPERATOR, parameters);

        Zombies plugin = Zombies.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player)context.getSender();
        Zombies.getInstance().getContextManager().getContextMap().put(player.getUniqueId(), new EditorContext(
                new MapData()));

        return "Created a new map with default values.";
    }
}