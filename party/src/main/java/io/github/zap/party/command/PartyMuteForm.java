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
import io.github.zap.party.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

/**
 * Mute a player or the party chat
 */
public class PartyMuteForm extends CommandForm<OfflinePlayer> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("mute"),
            new Parameter("\\w+", "[player-name]", "")
    };

    private static final CommandValidator<OfflinePlayer, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();

        Optional<Party> partyOptional = partyManager.getPartyForPlayer(previousData);
        if (partyOptional.isEmpty()) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        Party party = partyOptional.get();

        if (!party.isOwner(previousData)) {
            return ValidationResult.of(false, "You are not the party owner.", null);
        }

        String playerName = (String) arguments[1];
        if (previousData.getName().equalsIgnoreCase(playerName)) {
            return ValidationResult.of(false, "You cannot kick yourself.", null);
        }

        if (!playerName.equals("")) {
            OfflinePlayer toKick = Bukkit.getOfflinePlayerIfCached(playerName);
            if (toKick == null) {
                return ValidationResult.of(false, String.format("%s is not registered on the server!", playerName),
                        null);
            }

            Optional<Party> toKickPartyOptional = partyManager.getPartyForPlayer(toKick);
            if (toKickPartyOptional.isPresent()) {
                if (!party.equals(toKickPartyOptional.get())) {
                    return ValidationResult.of(false, String.format("%s is not in your party.", playerName), null);
                }
            }

            return ValidationResult.of(true, null, toKick);
        } else {
            return ValidationResult.of(true, null, null);
        }
    }, Validators.PLAYER_EXECUTOR);

    public PartyMuteForm() {
        super("Mutes a member in your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<OfflinePlayer, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, OfflinePlayer data) {
        Optional<Party> partyOptional = PartyPlusPlus.getInstance().getPartyManager()
                .getPartyForPlayer((OfflinePlayer) context.getSender());
        if (partyOptional.isPresent()) {
            Party party = partyOptional.get();
            if (data == null) {
                party.mute();
            } else {
                party.mutePlayer(data);
            }
        }

        return null;
    }

}

