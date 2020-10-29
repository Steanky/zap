package io.github.zap.command;

import io.github.regularcommands.validator.CommandValidator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;

public final class Validators {
    public static final CommandValidator PLAYER_EXECUTOR = new CommandValidator((context, arguments) -> {
        if(context.getSender() instanceof Player) {
            return ImmutablePair.of(true, null);
        }

        return ImmutablePair.of(false, "Only players can execute this command.");
    });
}
