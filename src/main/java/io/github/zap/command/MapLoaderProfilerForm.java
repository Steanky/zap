package io.github.zap.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Converters;
import io.github.regularcommands.converter.Parameter;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class MapLoaderProfilerForm extends CommandForm {
    private static final Parameter[] parameters = {
            new Parameter("^(profile)$", "profile"),
            new Parameter("^(maploader)$", "maploader"),
            new Parameter("^(\\d+)$", "[iterations]", Converters.INTEGER_CONVERTER),
            new Parameter("^([a-zA-Z0-9_]+)$", "[world]")
    };

    private static final CommandValidator validator;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Semaphore profilerSemaphore = new Semaphore(1);
    private static final Range<Integer> requiredRange = Range.between(1, 100);
    private static final StopWatch profiler = new StopWatch();

    static {
        validator = new CommandValidator((context, arguments) -> {
            String worldName = (String)arguments[3];
            if(ZombiesPlugin.getInstance().getMapLoader().worldExists(worldName)) {
                int iterations = (int)arguments[2];
                if(requiredRange.contains(iterations)) {
                    return ImmutablePair.of(true, null);
                }

                return ImmutablePair.of(false, String.format("Value '%s' out of range %s.", iterations,
                        requiredRange.toString()));
            }

            return ImmutablePair.of(false, String.format("World '%s' doesn't exist.", worldName));
        });

        validator.chain(Validators.PLAYER_EXECUTOR);
    }

    public MapLoaderProfilerForm() {
        super("Test command for profiling map loading.", Permissions.REQUIRE_OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        if(profilerSemaphore.tryAcquire()) {
            Player player = (Player)context.getSender(); //validation ensures that this will never throw ClassCastException
            int iterations = (int)arguments[2];
            String worldName = (String)arguments[3];

            ZombiesPlugin instance = ZombiesPlugin.getInstance();
            MapLoader loader = instance.getMapLoader();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            List<String> loadedWorlds = new ArrayList<>();

            Semaphore semaphore = new Semaphore(-(iterations - 1));
            executorService.submit(() -> {
                scheduler.runTask(instance, () -> player.sendMessage("===Start maploader profiling session==="));

                profiler.start();
                semaphore.acquireUninterruptibly();
                profiler.stop();

                scheduler.runTask(instance, () -> {
                    player.sendMessage(String.format("Loaded %s copies of world %s in ~%sms", iterations, worldName,
                            profiler.getTime()));
                    profiler.reset();
                    profilerSemaphore.release();

                    player.sendMessage("Cleaning up worlds.");
                    profiler.start();
                    for(String world : loadedWorlds) {
                        loader.unloadMap(world);
                    }
                    profiler.stop();
                    player.sendMessage(String.format("Done unloading worlds; ~%sms elapsed", profiler.getTime()));
                    player.sendMessage("===End maploader profiling session===");
                    profiler.reset();
                });
            });

            for(int i = 0; i < iterations; i++) {
                loader.loadMap(worldName, (world -> {
                    loadedWorlds.add(world.getName());
                    semaphore.release();
                }));
            }

            player.sendMessage("All loading tasks started.");
        }
        else {
            return "The profiler is already running.";
        }

        return null;
    }
}
