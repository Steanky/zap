package io.github.zap.zombies.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinZombiesGameForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("^(joinarena)$", "joinarena")
    };

    public JoinZombiesGameForm() {
        super("Joins a Zombies game.", Permissions.OPERATOR, parameters);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Player player = (Player) context.getSender();
        ArenaApi api = Zombies.getInstance().getArenaApi();
        JoinInformation testInformation = new JoinInformation(new SimpleJoinable(Lists.newArrayList(player)),
                "zombies", "test_map", null, null);

        api.handleJoin(testInformation, (pair) -> {
            if(!pair.left) {
                player.sendMessage(pair.right);
            }
        });

        return ">green{Attemping to join a game...}";
    }
}
