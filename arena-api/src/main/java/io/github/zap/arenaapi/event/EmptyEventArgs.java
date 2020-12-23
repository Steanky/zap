package io.github.zap.arenaapi.event;

import lombok.Getter;

/**
 * Used for event classes that don't pass any arguments. Basically just a placeholder value.
 */
public final class EmptyEventArgs {
    @SuppressWarnings("InstantiationOfUtilityClass")
    @Getter
    private static final EmptyEventArgs instance = new EmptyEventArgs();

    private EmptyEventArgs() {}
}
