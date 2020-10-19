package io.github.zap.map;

import com.google.common.collect.ImmutableSet;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Currently only used for testing purposes.
 */
public class TestData extends DataSerializable {
    @Getter
    @Serialize(name = "value")
    private int value;

    @Serialize(name = "values")
    private TestData2[][] values;

    @Serialize(name = "strings")
    private List<String> stringValues;

    /*
    when deserializing generic types containing array types, the array type gets replaced by an arraylist. do not try
    to serialize these values because as of right now the implementation cannot convert it properly
     */
    @Getter
    private List<TestData2[]> doNotSerialize;

    @Serialize(name = "set")
    private Set<TestData2> set;

    //you must include a parameterless constructor for all DataSerializable classes. it can be private
    private TestData() { }

    public TestData(int value) {
        this.value = value;
        values = new TestData2[][] {{new TestData2(10), new TestData2(11)}, {new TestData2(12)}};
        stringValues = new ArrayList<>();

        stringValues.add("this");
        stringValues.add("is");
        stringValues.add("a");
        stringValues.add("test");
        stringValues.add("value");

        set = ImmutableSet.of(new TestData2(100));
    }
}
