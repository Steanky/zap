package io.github.zap.arenaapi.serialize2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathParserTest {
    private static final String basic = "this is/a/well formed/path";
    private static final String drunk = "////this is///a path///that////someone///made////while///drunk////";

    private static final String[] basicExpected = new String[] {
            "this is",
            "a",
            "well formed",
            "path"
    };

    private static final String[] drunkExpected = new String[] {
            "this is",
            "a path",
            "that",
            "someone",
            "made",
            "while",
            "drunk"
    };

    @Test
    public void testStandardInput() {
        Assertions.assertArrayEquals(basicExpected, PathParser.path(basic));
    }

    @Test
    public void testDrunkInput() {
        Assertions.assertArrayEquals(drunkExpected, PathParser.path(drunk));
    }
}