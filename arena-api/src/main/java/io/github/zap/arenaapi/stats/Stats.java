package io.github.zap.arenaapi.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * General statistics usable for anything about arenas
 * @param <I> The type of the identifier for the stats
 */
@Data
@AllArgsConstructor
public class Stats<I> {

    private @NotNull I identifier;

    @SuppressWarnings("unused")
    protected Stats() {

    }

}
