package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.commands.PermissionData;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.ContextManager;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import org.bukkit.entity.Player;

public class ExitEditorForm extends CommandForm<Player> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("exit")
    };

    public ExitEditorForm() {
        super("Cancels a mapeditor session.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Player data) {
        ContextManager manager = Zombies.getInstance().getContextManager();

        if(manager.hasContext(data)) {
            EditorContext editorContext = Zombies.getInstance().getContextManager().removeContext(data);
            editorContext.setMap(null);
            editorContext.dispose();
            return "Ended mapeditor session.";
        }
        else {
            return "You do not have an active mapeditor session.";
        }
    }
}
