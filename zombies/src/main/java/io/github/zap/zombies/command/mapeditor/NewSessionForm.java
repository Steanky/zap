package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

public class NewSessionForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("^(session)$", "session"),
            new Parameter("^(start)$", "start")
    };

    public NewSessionForm() {
        super("Creates a new session with the mapeditor.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.NO_EDITOR_CONTEXT;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Zombies zombies = Zombies.getInstance();
        Player player = (Player)context.getSender();

        zombies.getContextManager().getContextMap().put(player.getUniqueId(), new EditorContext(player));
        return "Started a new mapeditor session.";
    }
}
