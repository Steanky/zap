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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Kicks all offline members from the party
 */
public class KickOfflineMembersForm extends CommandForm<Party> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("kickoffline")
    };

    private final PartyPlusPlus partyPlusPlus;

    private final CommandValidator<Party, ?> validator;

    public KickOfflineMembersForm(@NotNull PartyPlusPlus partyPlusPlus) {
        super("Kicks a member from your party.", Permissions.NONE, PARAMETERS);

        this.partyPlusPlus = partyPlusPlus;
        this.validator = new CommandValidator<>((context, arguments, previousData) -> {
            Optional<Party> partyOptional = partyPlusPlus.getPartyForPlayer(previousData);
            if (partyOptional.isEmpty()) {
                return ValidationResult.of(false, "You are not currently in a party.", null);
            }

            Party party = partyOptional.get();
            if (!party.isOwner(previousData)) {
                return ValidationResult.of(false, "You are not the party owner.", null);
            }

            return ValidationResult.of(true, null, party);
        }, Validators.PLAYER_EXECUTOR);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        data.kickOffline();
        return null;
    }


}
