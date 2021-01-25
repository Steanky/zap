package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

public class GiveWandForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("wand")
    };

    public GiveWandForm() {
        super("Gives the player the mapeditor wand.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        ContextManager contextManager = Zombies.getInstance().getContextManager();
        Player player = (Player)context.getSender();
        player.getInventory().addItem(contextManager.getEditorItem());

        return contextManager.getContextMap().containsKey(player.getUniqueId()) ? null :
                "Given mapeditor wand, but it won't work unless you create a session first!";
    }
}
