package io.github.zap.arenaapi.serialize2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class PathParserTest {
    private static final String basic = "this is/a/well formed/path";
    private static final String complicated = "////this is///a path///that////someone///made////while///drunk///";

    private static final String[] basicExpected = new String[] {
            "this is",
            "a",
            "well formed",
            "path"
    };

    private static final String[] complicatedExpected = new String[] {
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
    public void testComplicatedInput() {
        Assertions.assertArrayEquals(complicatedExpected, PathParser.path(complicated));
    }
}