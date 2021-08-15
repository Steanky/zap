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
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Transfers the party to another player
 */
public class TransferPartyForm extends CommandForm<Pair<Party, Player>> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("transfer"),
            new Parameter("\\w+", "[player-name]")
    };

    private final CommandValidator<Pair<Party, Player>, ?> validator;

    public TransferPartyForm(@NotNull PartyPlusPlus partyPlusPlus) {
        super("Transfers the party to another member.", Permissions.NONE, PARAMETERS);

        this.validator = new CommandValidator<>((context, arguments, previousData) -> {
            Optional<Party> partyOptional = partyPlusPlus.getPartyForPlayer(previousData);
            if (partyOptional.isEmpty()) {
                return ValidationResult.of(false, "You are not currently in a party.", null);
            }

            Party party = partyOptional.get();

            if (!party.isOwner(previousData)) {
                return ValidationResult.of(false, "You are not the party owner.", null);
            }

            String playerName = (String) arguments[1];
            if (previousData.getName().equalsIgnoreCase(playerName)) {
                return ValidationResult.of(false, "You cannot transfer the party to yourself.", null);
            }

            Player toTransfer = Bukkit.getPlayer(playerName);
            if (toTransfer == null) {
                return ValidationResult.of(false, String.format("%s is currently not online.", playerName), null);
            }

            Optional<Party> toTransferPartyOptional = partyPlusPlus.getPartyForPlayer(toTransfer);
            if (toTransferPartyOptional.isPresent()) {
                Party toTransferParty = toTransferPartyOptional.get();
                if (party.equals(toTransferParty)) {
                    return ValidationResult.of(true, null, Pair.of(party, toTransfer));
                }
            }

            return ValidationResult.of(false, String.format("%s is not in your party.", playerName), null);
        }, Validators.PLAYER_EXECUTOR);
    }

    @Override
    public CommandValidator<Pair<Party, Player>, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Pair<Party, Player> data) {
        data.getLeft().transferPartyToPlayer(data.getRight());
        return null;
    }

}
