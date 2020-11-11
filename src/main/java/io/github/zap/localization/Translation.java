package io.github.zap.localization;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@AllArgsConstructor
public class Translation extends DataSerializable {
    @Getter
    private final Map<String, String> mappings = new HashMap<>();

    @Getter
    private Locale locale;

    private Translation() {}
}
