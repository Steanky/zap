package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class JacksonDataContainerTest {
    private static record Nested(@NotNull String nested_string, @NotNull NestedNested nested_nested) { }

    private static record NestedNested(int nested_nested_integer) { }

    private static final ObjectMapper mapper = new ObjectMapper(); //this takes a long time to construct
    private static final double epsilon = 0.00001; //used to compare doubles

    private JacksonDataContainer container;

    @BeforeEach
    void setup() throws JsonProcessingException {
        container = new JacksonDataContainer(mapper, mapper.readTree(Json.TEST_JSON));
    }

    @Test
    void testNonexistentAccess() {
        Optional<Object> objectOptional = container.getObject(Object.class, "this object", "does not exist");
        Assertions.assertFalse(objectOptional.isPresent());
    }

    @Test
    void testStringAccess() {
        Optional<String> stringOptional = container.getString("string");
        Assertions.assertTrue(stringOptional.isPresent());
        Assertions.assertEquals("this is a string", stringOptional.orElse(""));
    }

    @Test
    void testIntAccess() {
        Optional<Integer> integerOptional = container.getInt("int");
        Assertions.assertTrue(integerOptional.isPresent());
        Assertions.assertEquals((Integer)69, integerOptional.orElse(0));
    }

    @Test
    void testDoubleAccess() {
        Optional<Double> doubleOptional = container.getDouble("double");
        Assertions.assertTrue(doubleOptional.isPresent());
        Assertions.assertEquals(100.420, doubleOptional.get(), epsilon);
    }

    @Test
    void testListStringAccess() {
        Optional<List<String>> listStringOptional = container.getObject(new TypeToken<>() {}, "list_string");
        Assertions.assertTrue(listStringOptional.isPresent());

        int count = 0;
        for(String value : listStringOptional.get()) {
            Assertions.assertEquals("element_" + count++, value);
        }
    }

    @Test
    void testListIntAccess() {
        Optional<List<Integer>> listIntegerOptional = container.getObject(new TypeToken<>() {}, "list_int");
        Assertions.assertTrue(listIntegerOptional.isPresent());

        int count = 0;
        for(int value : listIntegerOptional.get()) {
            Assertions.assertEquals(count++, value);
        }
    }

    @Test
    void testListDoubleAccess() {
        Optional<List<Double>> listDoubleOptional = container.getObject(new TypeToken<>() {}, "list_double");
        Assertions.assertTrue(listDoubleOptional.isPresent());

        int count = 0;
        for(double value : listDoubleOptional.get()) {
            Assertions.assertEquals(0.69 + count++, value, epsilon);
        }
    }

    @Test
    void testNestedStringAccess() {
        Optional<String> stringOptional = container.getString("nested", "nested_string");
        Assertions.assertTrue(stringOptional.isPresent());
        Assertions.assertEquals("this is a nested string", stringOptional.get());
    }

    @Test
    void testNestedNestedIntegerAccess() {
        Optional<Integer> integerOptional = container.getInt("nested", "nested_nested", "nested_nested_integer");
        Assertions.assertTrue(integerOptional.isPresent());
        Assertions.assertEquals(69420, (int)integerOptional.get());
    }

    @Test
    void testObjectDeserializationThroughClass() {
        Optional<Nested> nestedOptional = container.getObject(Nested.class, "nested");
        Assertions.assertTrue(nestedOptional.isPresent());
    }

    @Test
    void testObjectDeserializationThroughTypeToken() {
        Optional<Nested> nestedOptional = container.getObject(new TypeToken<>() {}, "nested");
        Assertions.assertTrue(nestedOptional.isPresent());
    }

    @Test
    void testNestedObjectDeserialization() {
        Optional<NestedNested> nestedNestedOptional = container.getObject(new TypeToken<>(){}, "nested", "nested_nested");
        Assertions.assertTrue(nestedNestedOptional.isPresent());
        Assertions.assertEquals(69420, nestedNestedOptional.get().nested_nested_integer);
    }
}
