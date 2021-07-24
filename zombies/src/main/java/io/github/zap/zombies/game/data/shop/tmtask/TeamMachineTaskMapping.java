package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.TeamMachineTask;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link TeamMachineTask}s
 * @param <D> The type of the data for the task
 */
@FunctionalInterface
public interface TeamMachineTaskMapping<D extends @NotNull TeamMachineTaskData> {

    /**
     * Creates the {@link TeamMachineTask}
     * @return The task
     */
    @NotNull TeamMachineTask<D> createTeamMachineTask(@NotNull D teamMachineTaskData);

}
