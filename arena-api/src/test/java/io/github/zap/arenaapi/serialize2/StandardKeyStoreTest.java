package io.github.zap.arenaapi.serialize2;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StandardKeyStoreTest {
    static KeyStore keyStore;
    static DataMarshal marshal;
    static DataKey key1;
    static DataKey key2;
    static DataKey key3;

    static DataKey key4;
    static DataKey AAAAAAAAA;
    static DataKey why;

    static {
        keyStore = KeyStore.from("test");
        marshal = DataMarshal.standard();
        key1 = keyStore.named("vegetals");
        key2 = keyStore.named("angery");
        key3 = keyStore.named("suque");
        key4 = keyStore.named("beaned");
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
        recursiveAAAAAAAAA.put(AAAAAAAAA.key(), objectMap);

        objectMap1.put(key4.key(), "GIVE ME THE SUCC");

        objectMap.put(key1.key(), "NO VEGETALS!");
        objectMap.put(key2.key(), 69420);
        objectMap.put(key3.key(), objectMap1);
        objectMap.put(why.key(), recursiveAAAAAAAAA);

        DataContainer dataContainer = marshal.marshalData(objectMap);
    }
}