package io.github.zap.arenaapi.localization;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@AllArgsConstructor
public class Translation {
    @Getter
    private final Map<String, String> mappings = new HashMap<>();

    @Getter
    private final Locale locale;

    private final String fileName;

    public String getFileName() {
        return (fileName == null) ? locale.toLanguageTag() : fileName;
    }
}
