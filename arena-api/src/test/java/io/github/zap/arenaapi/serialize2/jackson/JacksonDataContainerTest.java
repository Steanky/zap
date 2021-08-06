package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class JacksonDataContainerTest {
    private static record Nested(@NotNull String nested_string, @NotNull NestedNested nested_nested) { }

    private static record NestedNested(int nested_nested_integer) { }

    private static final String TEST_JSON = "{ \"string\" : \"this is a string\", \"int\" : 69, \"double\" : 100.420," +
            " \"list_string\" : [\"element_0\", \"element_1\", \"element_2\"], \"list_int\" : [0, 1, 2, 3]," +
            " \"list_double\" : [0.69, 1.69, 2.69, 3.69], \"nested\" : { \"nested_string\" : \"this is a nested" +
            " string\", \"nested_nested\" : { \"nested_nested_integer\" : 69420 }}}";

    private static final ObjectMapper mapper = new ObjectMapper(); //this takes a long time to construct
    private static final double epsilon = 0.00001; //used to compare doubles

    private JacksonDataContainer container;

    @Before
    public void setup() throws JsonProcessingException {
        container = new JacksonDataContainer(mapper, mapper.readTree(TEST_JSON));
    }

    @Test
    public void testNonexistentAccess() {
        Optional<Object> objectOptional = container.getObject(Object.class, "this object", "does not exist");
        Assert.assertFalse(objectOptional.isPresent());
    }

    @Test
    public void testStringAccess() {
        Optional<String> stringOptional = container.getString("string");
        Assert.assertTrue(stringOptional.isPresent());
        Assert.assertEquals("this is a string", stringOptional.orElse(""));
    }

    @Test
    public void testIntAccess() {
        Optional<Integer> integerOptional = container.getInt("int");
        Assert.assertTrue(integerOptional.isPresent());
        Assert.assertEquals((Integer)69, integerOptional.orElse(0));
    }

    @Test
    public void testDoubleAccess() {
        Optional<Double> doubleOptional = container.getDouble("double");
        Assert.assertTrue(doubleOptional.isPresent());
        Assert.assertEquals(100.420, doubleOptional.get(), epsilon);
    }

    @Test
    public void testListStringAccess() {
        Optional<List<String>> listStringOptional = container.getObject(new TypeToken<>() {}, "list_string");
        Assert.assertTrue(listStringOptional.isPresent());

        int count = 0;
        for(String value : listStringOptional.get()) {
            Assert.assertEquals("element_" + count++, value);
        }
    }

    @Test
    public void testListIntAccess() {
        Optional<List<Integer>> listIntegerOptional = container.getObject(new TypeToken<>() {}, "list_int");
        Assert.assertTrue(listIntegerOptional.isPresent());

        int count = 0;
        for(int value : listIntegerOptional.get()) {
            Assert.assertEquals(count++, value);
        }
    }

    @Test
    public void testListDoubleAccess() {
        Optional<List<Double>> listDoubleOptional = container.getObject(new TypeToken<>() {}, "list_double");
        Assert.assertTrue(listDoubleOptional.isPresent());

        int count = 0;
        for(double value : listDoubleOptional.get()) {
            Assert.assertEquals(0.69 + count++, value, epsilon);
        }
    }

    @Test
    public void testNestedStringAccess() {
        Optional<String> stringOptional = container.getString("nested", "nested_string");
        Assert.assertTrue(stringOptional.isPresent());
        Assert.assertEquals("this is a nested string", stringOptional.get());
    }

    @Test
    public void testNestedNestedIntegerAccess() {
        Optional<Integer> integerOptional = container.getInt("nested", "nested_nested", "nested_nested_integer");
        Assert.assertTrue(integerOptional.isPresent());
        Assert.assertEquals(69420, (int)integerOptional.get());
    }

    @Test
    public void testObjectDeserializationThroughClass() {
        Optional<Nested> nestedOptional = container.getObject(Nested.class, "nested");
        Assert.assertTrue(nestedOptional.isPresent());
    }

    @Test
    public void testObjectDeserializationThroughTypeToken() {
        Optional<Nested> nestedOptional = container.getObject(new TypeToken<>() {}, "nested");
        Assert.assertTrue(nestedOptional.isPresent());
    }
}