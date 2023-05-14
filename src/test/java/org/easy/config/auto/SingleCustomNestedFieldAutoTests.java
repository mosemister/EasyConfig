package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SingleCustomNestedFieldAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        Map<String, Object> innerLoad = new HashMap<>();
        innerLoad.put("innerFieldTest", true);
        toLoad.put("fieldTest", innerLoad);

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertTrue(clazz.fieldTest.innerFieldTest);
    }

    @Test
    public void testSerialize() {
        InnerClass inner = new InnerClass(true);
        TestClass toSerialize = new TestClass(inner);
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
        Assertions.assertInstanceOf(Map.class, entry.getValue());
        Map<?, ?> innerMap = (Map<?, ?>) entry.getValue();
        Assertions.assertEquals(1, innerMap.size());
        Map.Entry<?, ?> innerEntry = innerMap.entrySet().iterator().next();
        Assertions.assertEquals("innerFieldTest", innerEntry.getKey());
        Assertions.assertEquals(true, innerEntry.getValue());
    }

    public static class TestClass {

        private final InnerClass fieldTest;

        @ConfigConstructor
        public TestClass(InnerClass fieldTest) {
            this.fieldTest = fieldTest;
        }
    }

    public static class InnerClass {

        private final boolean innerFieldTest;

        @ConfigConstructor
        public InnerClass(boolean innerFieldTest) {
            this.innerFieldTest = innerFieldTest;
        }
    }
}
