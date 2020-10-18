package io.github.zap.map;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;

/**
 * Currently only used for testing purposes.
 */
public class TestData implements DataSerializable {
    @Getter
    @Serialize(name = "value")
    private int value;

    @Getter
    @Serialize(name = "data")
    private TestData2[][] data;

    //you must include a parameterless constructor for all DataSerializable classes. it can be private
    private TestData() { }

    public TestData(int value) {
        this.value = value;
        data = new TestData2[][] { new TestData2[]{new TestData2("test")}, new TestData2[]{new TestData2("test")} };
    }
}
