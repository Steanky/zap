package io.github.zap.map;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;

public class TestData2 extends DataSerializable {
    //this won't get serialized because it doesn't have the @Serialize annotation
    @Getter
    private int zzz = 879543246;

    //this won't get serialized because it's static
    @Getter
    @Serialize(name = "this_wont_get_serialized")
    private static int z = 456789;

    //this will get serialized
    @Serialize(name = "value")
    private int value;

    private TestData2() {}

    public TestData2(int value) {
        this.value = value;
    }
}
