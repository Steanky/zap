package io.github.zap.arenaapi.util;

import java.io.File;
import java.util.function.Consumer;

public final class FileUtils {
    /**
     * Iterates through every file in the given directory, calling the provided consumer for each one.
     * @param directory The directory to iterate through
     * @param consumer The consumer to use
     */
    public static void forEachFile(File directory, Consumer<File> consumer) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                consumer.accept(file);
            }
        }
    }
}
