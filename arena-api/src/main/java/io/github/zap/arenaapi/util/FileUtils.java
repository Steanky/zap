package io.github.zap.arenaapi.util;

import java.io.File;
import java.util.function.Consumer;

public final class FileUtils {
    public static void forEachFile(File directory, Consumer<File> consumer) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                consumer.accept(file);
            }
        }
    }
}
