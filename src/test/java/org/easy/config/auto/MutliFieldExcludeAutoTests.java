package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MutliFieldExcludeAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", 1);

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, clazz.fieldTest);
    }

    @Test
    public void testSerialize() {
        TestClass toSerialize = new TestClass(1);
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act
        Map<String, Object> entries;
        try {
            entries = serializer.serialize(toSerialize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals(1, entries.size());
        Assertions.assertTrue(entries.containsKey("fieldTest"));
        Assertions.assertEquals(1, entries.get("fieldTest"));
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    public static class TestClass {

        private final int fieldTest;
        @ConfigField(exclude = true)
        private final boolean test;

        @ConfigConstructor
        public TestClass(int fieldTest) {
            this.fieldTest = fieldTest;
            test = false;
        }
    }
}
