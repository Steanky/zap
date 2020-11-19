package io.github.zap.arenaapi.serialize;

import lombok.Value;

@Value
public class ClassEntry {
    String className;
    boolean serialize;
}
