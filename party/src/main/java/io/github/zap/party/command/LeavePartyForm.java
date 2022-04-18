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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Leaves your current party
 */
public class LeavePartyForm extends CommandForm<Void> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("leave")
    };

    private final PartyPlusPlus partyPlusPlus;

    private final CommandValidator<Void, ?> validator;

    public LeavePartyForm(@NotNull PartyPlusPlus partyPlusPlus) {
        super("Leaves your party.", Permissions.NONE, PARAMETERS);

        this.partyPlusPlus = partyPlusPlus;
        this.validator = new CommandValidator<>((context, arguments, previousData) -> {
            Optional<Party> party = partyPlusPlus.getPartyForPlayer(previousData);
            if (party.isEmpty()) {
                return ValidationResult.of(false, "You are not currently in a party.", null);
            }

            return ValidationResult.of(true, null, null);
        }, Validators.PLAYER_EXECUTOR);
    }

    @Override
    public CommandValidator<Void, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Void data) {
        OfflinePlayer sender = (OfflinePlayer) context.getSender();
        partyPlusPlus.getPartyForPlayer(sender).ifPresent(party -> party.removeMember(sender, false));
        return null;
    }

}
