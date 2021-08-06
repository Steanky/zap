package io.github.zap.party;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.party.command.PartyCommand;
import io.github.zap.party.party.PartyManager;
import io.github.zap.party.party.PartyMember;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Creates cool parties for you!
 */
public class PartyPlusPlus extends JavaPlugin {

    @Getter
    private static PartyPlusPlus instance;

    @Getter
    private PartyManager partyManager;

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();

        instance = this;

        initPartyManager();
        initCommands();

        timer.stop();
        instance.getLogger().log(Level.INFO, String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    /**
     * Initializes the party manager
     */
    private void initPartyManager() {
        PartyManager partyManager = new PartyManager(Bukkit.getScheduler());
        Bukkit.getPluginManager().registerEvents(partyManager, this);
    }

    /**
     * Registers the command manager
     */
    private void initCommands() {
        commandManager = new CommandManager(this);
        commandManager.registerCommand(new PartyCommand());
    }

}
