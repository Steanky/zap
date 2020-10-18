package io.github.zap.map;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;

public class TestData2 implements DataSerializable {
    @Getter
    @Serialize(name = "value2")
    private String value;

    private TestData2() {}

    public TestData2(String value) {
        this.value = value;
    }
}
