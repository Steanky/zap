package io.github.zap.zombies.command;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

public class JoinZombiesGameForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("join"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[arena-name]"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[map-name]")
    };

    private static final CommandValidator validator;

    static {
        validator = new CommandValidator((context, form, arguments) -> {
            String managerName = (String)arguments[1];
            String mapName = (String)arguments[2];

            ArenaManager<?> arenaManager = ArenaApi.getInstance().getArenaManager(managerName);

            if(arenaManager == null) {
                return ValidationResult.of(false, String.format("An ArenaManager named '%s' does not exist.",
                        managerName));
            }

            if(!arenaManager.hasMap(mapName)) {
                return ValidationResult.of(false, String.format("A map named '%s' does not exist for " +
                        "ArenaManager '%s'", mapName, managerName));
            }

            return ValidationResult.of(true, null);
        }, Validators.PLAYER_EXECUTOR);
    }

    public JoinZombiesGameForm() {
        super("Joins a Zombies game.", Permissions.OPERATOR, parameters);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player) context.getSender();
        ArenaApi api = Zombies.getInstance().getArenaApi();
        JoinInformation testInformation = new JoinInformation(new SimpleJoinable(Lists.newArrayList(player)),
                (String)arguments[1], (String)arguments[2], null, null);

        api.handleJoin(testInformation, (pair) -> {
            if(!pair.left) {
                player.sendMessage(pair.right);
            }
        });

        return ">green{Attemping to join a game...}";
    }
}
