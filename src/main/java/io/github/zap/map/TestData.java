package io.github.zap.map;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

/**
 * Currently only used for testing purposes.
 */
public class TestData implements DataSerializer<TestData> {
    //let data classes contain their own deserializer as a field. this is registered in ZombiesPlugin
    @Getter
    private static final DataDeserializer<TestData> deserializer = (data) -> new TestData((int)data.get("value"));

    @Getter
    private final int value;

    public TestData(int value) {
        this.value = value;
    }

    @Override
    public Map<String, Object> serialize() {
        /*
        it is necessary to specify the typeClass field, as this is what we will use
        to identify deserializer during deserialization
        */
        return ImmutableMap.of("typeClass", TestData.class.getTypeName(), "value", value);
    }
}
