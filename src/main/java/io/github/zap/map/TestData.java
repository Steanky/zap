package io.github.zap.map;

import io.github.zap.serialize.DataDeserializer;
import io.github.zap.serialize.DataSerializer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Currently only used for testing purposes.
 */
public class TestData implements DataSerializer {
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
        map must not be immutable because the DataWrapper will add a special value to it
         */
        HashMap<String, Object> map = new HashMap<>();
        map.put("value", value);

        return map;
    }
}
