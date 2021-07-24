package io.github.zap.zombies.game.data.shop.tmtask;

import org.jetbrains.annotations.NotNull;

/**
 * A pair of an {@link TeamMachineTaskData} and a {@link TeamMachineTaskMapping} that creates an associated {@link io.github.zap.zombies.game.shop.tmtask.TeamMachineTask}
 * @param <D> The type of the team machine task data
 * @param <M> The type of the mapping
 */
public record TeamMachineTaskDataMappingPair<D extends @NotNull TeamMachineTaskData, M extends @NotNull TeamMachineTaskMapping<D>>(D data, M mapping) {
}
