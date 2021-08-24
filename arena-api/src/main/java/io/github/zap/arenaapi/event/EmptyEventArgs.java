package io.github.zap.arenaapi.event;

import org.jetbrains.annotations.NotNull;

/**
 * Used for event classes that don't pass any arguments. Basically just a placeholder value.
 */
public final class EmptyEventArgs {
    @SuppressWarnings("InstantiationOfUtilityClass")
    private static final EmptyEventArgs instance = new EmptyEventArgs();

    private EmptyEventArgs() {}

    public static @NotNull EmptyEventArgs getInstance() {
        return instance;
    }
}
