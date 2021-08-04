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

    @NotNull String[] parse(@NotNull String input);

    static @NotNull String[] path(@NotNull String input) {
        return STANDARD.parse(input);
    }
}
