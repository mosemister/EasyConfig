package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MutliFieldAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", 1);
        toLoad.put("test", true);

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, clazz.fieldTest);
        Assertions.assertEquals(true, clazz.test);
    }

    @Test
    public void testSerialize() {
        TestClass toSerialize = new TestClass(1, true);
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act
        Map<String, Object> entries;
        try {
            entries = serializer.serialize(toSerialize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(2, entries.size());
        Assertions.assertTrue(entries.containsKey("fieldTest"));
        Assertions.assertTrue(entries.containsKey("test"));
        Assertions.assertEquals(1, entries.get("fieldTest"));
        Assertions.assertEquals(true, entries.get("test"));
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    public static class TestClass {

        private final int fieldTest;
        private final boolean test;

        @ConfigConstructor
        public TestClass(int fieldTest, boolean test) {
            this.fieldTest = fieldTest;
            this.test = test;
        }
    }
}
