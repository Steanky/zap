package io.github.zap.party.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.party.party.Party;
import io.github.zap.party.party.creator.PartyCreator;
import io.github.zap.party.plugin.tracker.PartyTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Creates a new party
 */
public class CreatePartyForm extends CommandForm<Void> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("create")
    };

    private final PartyTracker partyTracker;

    private final PartyCreator partyCreator;

    private final CommandValidator<Void, ?> validator;

    public CreatePartyForm(@NotNull PartyTracker partyTracker, @NotNull PartyCreator partyCreator) {
        super("Creates a party.", Permissions.NONE, PARAMETERS);

        this.partyTracker = partyTracker;
        this.partyCreator = partyCreator;
        this.validator = new CommandValidator<>((context, arguments, previousData) -> {
            Optional<Party> partyOptional = partyTracker.getPartyForPlayer((OfflinePlayer) context.getSender());

            if (partyOptional.isPresent()) {
                return ValidationResult.of(false, "You are already in a party.", null);
            }

            return ValidationResult.of(true, null, null);
        }, Validators.PLAYER_EXECUTOR);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<Void, ?> getValidator(Context context, Object[] arguments) {
        return this.validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Void data) {
        this.partyTracker.trackParty(this.partyCreator.createParty((Player) context.getSender()));
        return ">gold{Created a new party.}";
    }

}
