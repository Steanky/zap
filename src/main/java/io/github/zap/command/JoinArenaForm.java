package io.github.zap.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.ZombiesPlugin;
import io.github.zap.util.ChannelNames;
import org.bukkit.Bukkit;

public class JoinArenaForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("^(join)$", "join"),
            new Parameter("^(arena)$", "game"),
            new Parameter("^([a-zA-Z0-9_]+)$", "[game_name]"),
            new Parameter("^([a-zA-Z0-9_]+)$", "[map_name]"),
            new Parameter("^(true|false)$", "[true|false]", Converters.BOOLEAN_CONVERTER),
    };

    public JoinArenaForm() {
        super("Test command for joining arenas.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public String execute(Context context, Object[] arguments) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        //bungeecord Forward protocol
        output.writeUTF("Forward");
        output.writeUTF("ALL");

        //custom protocol
        output.writeUTF("JOIN");

        output.writeUTF(Bukkit.getServer().getName());
        output.writeUTF((context.getSender()).getName());
        output.writeUTF((String)arguments[2]);
        output.writeUTF((String)arguments[3]);
        output.writeBoolean((boolean)arguments[4]);
        output.writeUTF("null");

        ZombiesPlugin.getInstance().getServer().sendPluginMessage(ZombiesPlugin.getInstance(),
                ChannelNames.BUNGEECORD, output.toByteArray());

        return "Sent join request to server manager.";
    }
}
