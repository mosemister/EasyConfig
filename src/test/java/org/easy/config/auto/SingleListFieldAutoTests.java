package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleListFieldAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> internal = new HashMap<>();
        internal.put("example", true);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("fieldTest", Collections.singletonList(internal));

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertFalse(clazz.fieldTest.isEmpty());
        Assertions.assertTrue(clazz.fieldTest.get(0).example);
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
        List<Map<String, Object>> listValue = (List<Map<String, Object>>) entry.getValue();
        Assertions.assertEquals(listValue.size(), 1);
        entries = listValue.get(0);
        Assertions.assertEquals(entries.size(), 1);
        entry = entries.entrySet().iterator().next();
        Assertions.assertEquals(entry.getKey(), "example");
        Assertions.assertEquals(entry.getValue(), true);
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    private static class InternalClass {


        private final boolean example;

        @ConfigConstructor
        public InternalClass(boolean example) {
            this.example = example;
        }
    }

    public static class TestClass {

        @ConfigList(ofType = InternalClass.class)
        private final List<InternalClass> fieldTest;

        private TestClass(Boolean... values) {
            this(Stream.of(values).map(InternalClass::new).collect(Collectors.toList()));
        }

        @ConfigConstructor
        private TestClass(Collection<InternalClass> fieldTest) {
            this.fieldTest = new ArrayList<>(fieldTest);
        }
    }
}
