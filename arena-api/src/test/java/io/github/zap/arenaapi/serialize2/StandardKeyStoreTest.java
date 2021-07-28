package io.github.zap.arenaapi.serialize2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

public class StandardKeyStoreTest {
    static KeyStore keyStore;
    static DataMarshal marshal;
    static DataKey key1;
    static DataKey key2;
    static DataKey key3;

    static DataKey key4;
    static DataKey key69;
    static DataKey AAAAAAAAA;
    static DataKey why;

    static {
        keyStore = KeyStore.from("test");
        marshal = DataMarshal.from(KeyFactory.standard());
        key1 = keyStore.named("vegetals");
        key2 = keyStore.named("angery");
        key3 = keyStore.named("suque");
        key4 = keyStore.named("beaned");
        key69 = keyStore.named("funny number");
        AAAAAAAAA = keyStore.named("AAAAAAAAAAAAAAAAAA");
        why = keyStore.named("why would you do this");
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void test() {
        Map<String, Object> objectMap = new HashMap<>();
        Map<String, Object> objectMap1 = new HashMap<>();
        Map<String, Object> recursiveAAAAAAAAA = new HashMap<>();
        Map<String, Object> mapContainingInvalidKeys = new HashMap<>();
        recursiveAAAAAAAAA.put(AAAAAAAAA.key(), objectMap);
        mapContainingInvalidKeys.put("this is not a valid key for standard datamarshal", "HAHAHAHAHAHAHA"); //this won't get converted to DataContainer

        objectMap1.put(key4.key(), "GIVE ME THE SUCC");

        objectMap.put(key1.key(), "NO VEGETALS!");
        objectMap.put(key2.key(), 69420);
        objectMap.put(key3.key(), objectMap1);
        objectMap.put(why.key(), recursiveAAAAAAAAA);
        objectMap.put(key69.key(), mapContainingInvalidKeys);

        DataContainer dataContainer = marshal.fromMappings(objectMap);

        String json = "{\"test:name\" : \"value\", \"test:nested\" : {\"test:name\" : \"value\"}, \"test:list\" : [ [ 10, 10, 10 ], [ 10, 10, 10 ], [ 10, 10, 10 ] ]}";

        Gson gson = new Gson();

        //noinspection unchecked
        Map<String, Object> map = gson.fromJson(json, Map.class);
        DataContainer loaded = marshal.fromMappings(map);

        Optional<List<List<Integer>>> string = loaded.getObject(new TypeInformation(LinkedList.class,
                new TypeInformation(LinkedList.class, new TypeInformation(Integer.class))), keyStore.named("list"));
    }
}