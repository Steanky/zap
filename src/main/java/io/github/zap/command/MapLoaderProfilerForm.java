package io.github.zap.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.CommandManager;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.commands.PermissionData;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.ZombiesPlugin;
import io.github.zap.maploader.MapLoader;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MapLoaderProfilerForm extends CommandForm {
    private static final Parameter[] parameters = {
            new Parameter("^(profile)$", "profile"),
            new Parameter("^(maploader)$", "maploader"),
            new Parameter("^(\\d+)$", "[iterations]", Converters.INTEGER_CONVERTER),
            new Parameter("^([a-zA-Z0-9_]+)$", "[world]")
    };

    private static final CommandValidator validator;
    private static final Semaphore profilerSemaphore = new Semaphore(1);
    private static final StopWatch profiler = new StopWatch();

    static {
        validator = new CommandValidator((context, arguments) -> {
            String worldName = (String)arguments[3];
            if(ZombiesPlugin.getInstance().getMapLoader().worldExists(worldName)) {
                return ImmutablePair.of(true, null);
            }

            return ImmutablePair.of(false, String.format("World '%s' doesn't exist.", worldName));
        });

        validator.chain(Validators.newRangeValidator(Range.between(1, 50), 2)).chain(Validators.PLAYER_EXECUTOR);
    }

    public MapLoaderProfilerForm() {
        super("Debug command for profiling map loading.", new PermissionData(true), parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        if(profilerSemaphore.tryAcquire()) { //only one instance of the profiler can run at a time
            Player player = (Player)context.getSender(); //validation ensures that this will never throw ClassCastException
            int iterations = (int)arguments[2];
            String worldName = (String)arguments[3];

            ZombiesPlugin instance = ZombiesPlugin.getInstance();
            CommandManager commandManager = context.getManager();
            MapLoader loader = instance.getMapLoader();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            List<String> loadedWorlds = new ArrayList<>();

            Semaphore semaphore = new Semaphore(-(iterations - 1));
            scheduler.runTaskAsynchronously(instance, () -> {
                scheduler.runTask(instance, () -> commandManager.sendStylizedMessage(player,
                        ">green{===Start maploader profiling session===}"));

                profiler.start();
                semaphore.acquireUninterruptibly();
                profiler.stop();

                scheduler.runTask(instance, () -> {
                    commandManager.sendStylizedMessage(player, String.format("Loaded >green{%s} copies of world " +
                                    ">green{%s} in >green{~%sms}", iterations, worldName, profiler.getTime()));
                    profiler.reset();

                    commandManager.sendStylizedMessage(player, "Cleaning up worlds.");

                    profiler.start();
                    for(String world : loadedWorlds) {
                        loader.unloadMap(world);
                    }
                    profiler.stop();

                    player.sendMessage(String.format("Done unloading worlds; ~%sms elapsed", profiler.getTime()));
                    player.sendMessage("===End maploader profiling session===");

                    profiler.reset();
                    profilerSemaphore.release();
                });
            });

            for(int i = 0; i < iterations; i++) {
                loader.loadMap(worldName, world -> {
                    loadedWorlds.add(world.getName());
                    semaphore.release();
                });
            }
        }
        else {
            return "The profiler is already running.";
        }

        return null;
    }
}
