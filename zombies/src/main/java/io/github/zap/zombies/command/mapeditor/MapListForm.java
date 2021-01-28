package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;

public class MapListForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("list")
    };

    public MapListForm() {
        super("Lists all maps that currently exist.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player)context.getSender();

        for(MapData map : Zombies.getInstance().getArenaManager().getMaps()) {
            player.sendMessage(map.getName());
        }

        return null;
    }
}
