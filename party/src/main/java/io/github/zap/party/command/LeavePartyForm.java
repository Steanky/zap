package io.github.zap.party.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.party.PartyPlusPlus;
import io.github.zap.party.party.Party;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

/**
 * Leaves your current party
 */
public class LeavePartyForm extends CommandForm<Void> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("leave")
    };

    private static final CommandValidator<Void, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        Optional<Party> party = PartyPlusPlus.getInstance().getPartyManager().getPartyForPlayer(previousData);
        if (party.isEmpty()) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        return ValidationResult.of(true, null, null);
    }, Validators.PLAYER_EXECUTOR);

    public LeavePartyForm() {
        super("Leaves your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Void, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Void data) {
        PartyPlusPlus.getInstance().getPartyManager().removePlayerFromParty((OfflinePlayer) context.getSender(),
                false);
        return null;
    }

}
