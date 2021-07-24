package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.TeamMachineTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates {@link TeamMachineTask}s from its {@link TeamMachineTaskData}
 */
public interface TeamMachineTaskCreator {

    @Nullable <D extends @NotNull TeamMachineTaskData> TeamMachineTask<D> createTeamMachineTask(@NotNull D teamMachineTaskData);

}
