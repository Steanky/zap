package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

public interface PathParser {
    PathParser STANDARD = new StandardParser();

    class StandardParser implements PathParser {
        @Override
        public @NotNull String[] parse(@NotNull String input) {
            return input.split("/");
        }
    }

    /**
     * Attempts to parse the given input string into an array of strings, which may be used as a "data path" for
     * DataContainer instances.
     *
     * If the path syntax is invalid, however that is defined, an exception may be thrown.
     * @param input The input string
     * @return An array of strings, which can be empty to represent the top level
     */
    @NotNull String[] parse(@NotNull String input);

    /**
     * Parses a path string according to standard rules (/ as a path separator).
     * @param input The raw input string
     * @return An array of strings corresponding to each node along the path
     */
    static @NotNull String[] path(@NotNull String input) {
        return STANDARD.parse(input);
    }
}
