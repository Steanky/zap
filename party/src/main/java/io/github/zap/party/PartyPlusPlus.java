package io.github.zap.party;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.party.party.PartyManager;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Creates cool parties for you!
 */
public class PartyPlusPlus extends JavaPlugin {

    @Getter
    private static PartyPlusPlus instance;

    @Getter
    private final PartyManager partyManager = new PartyManager();

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        timer.start();

        instance = this;

        timer.stop();
        instance.getLogger().log(Level.INFO, String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    /**
     * Registers the command manager
     */
    private void initCommands() {
        commandManager = new CommandManager(this);
    }

}
