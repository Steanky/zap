package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;

public final class MapeditorValidators {
    public static final CommandValidator HAS_EDITOR_CONTEXT = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        if(!zombies.getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
            return new ImmutablePair<>(false, "You are not currently editing a map.");
        }

        return new ImmutablePair<>(true, null);
    });

    public static final CommandValidator NO_EDITOR_CONTEXT = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        if(zombies.getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
            return new ImmutablePair<>(false, "You are already editing a map.");
        }

        return new ImmutablePair<>(true, null);
    });

    static {
        HAS_EDITOR_CONTEXT.chain(Validators.PLAYER_EXECUTOR);
        NO_EDITOR_CONTEXT.chain(Validators.PLAYER_EXECUTOR);
    }
}
