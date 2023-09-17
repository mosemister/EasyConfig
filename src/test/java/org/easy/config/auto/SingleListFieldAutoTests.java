package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class SingleListFieldAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", Collections.singletonList(true));

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertFalse(clazz.fieldTest.isEmpty());
        Assertions.assertTrue(clazz.fieldTest.get(0));
    }

    @Test
    public void testInvalidLoadWithBadKey() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("badKey", true);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(toLoad));
    }

    @Test
    public void testInvalidLoadWithNoKey() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(toLoad));
    }

    @Test
    public void testInvalidLoadWithNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(null));
    }

    @Test
    public void testSerialize() {
        TestClass toSerialize = new TestClass(true);
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
        Map.Entry<String, Object> entry = entries.entrySet().iterator().next();
        Assertions.assertEquals("fieldTest", entry.getKey());
        Assertions.assertEquals(Collections.singletonList(true), entry.getValue());
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    public static class TestClass {

        private final List<Boolean> fieldTest;

        private TestClass(Boolean... values) {
            this(Arrays.asList(values));
        }

        @ConfigConstructor
        private TestClass(Collection<Boolean> fieldTest) {
            this.fieldTest = new ArrayList<>(fieldTest);
        }
    }
}
