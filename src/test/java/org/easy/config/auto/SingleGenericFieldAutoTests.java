package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SingleGenericFieldAutoTests {

    @Test
    public void testLoadInteger() {
        AutoSerializer<TestClass<Integer>> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", 1);

        //act
        TestClass<Integer> clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, clazz.fieldTest);
    }

    @Test
    public void testLoadDouble() {
        AutoSerializer<TestClass<Double>> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", 1.0);

        //act
        TestClass<Double> clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1.0, clazz.fieldTest);
    }

    @Test
    public void testIntegerSerialize() {
        TestClass<Integer> toSerialize = new TestClass<>(1);
        AutoSerializer<TestClass<Integer>> serializer = new AutoSerializer<>(TestClass.class);

        //act
        Map<String, Object> entries;
        try {
            entries = serializer.serialize(toSerialize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, entries.size());
        Map.Entry<String, Object> entry = entries.entrySet().iterator().next();
        Assertions.assertEquals("fieldTest", entry.getKey());
        Assertions.assertEquals(1, entry.getValue());
    }

    @Test
    public void testDoubleSerialize() {
        TestClass<Double> toSerialize = new TestClass<>(1.0);
        AutoSerializer<TestClass<Double>> serializer = new AutoSerializer<>(TestClass.class);

        //act
        Map<String, Object> entries;
        try {
            entries = serializer.serialize(toSerialize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, entries.size());
        Map.Entry<String, Object> entry = entries.entrySet().iterator().next();
        Assertions.assertEquals("fieldTest", entry.getKey());
        Assertions.assertEquals(1.0, entry.getValue());
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass<Integer>> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    public static class TestClass<T extends Number> {

        @ConfigField
        private final T fieldTest;

        @ConfigConstructor
        public TestClass(T fieldTest) {
            this.fieldTest = fieldTest;
        }
    }
}
