package org.easy.config.auto;

import org.easy.config.auto.annotations.ConfigConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SingleExtendingFieldAutoTests {

    @Test
    public void testLoad() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);
        Map<String, Object> toLoad = new HashMap<>();
        toLoad.put("test", true);
        toLoad.put("testing", true);

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertTrue(clazz.testing);
        Assertions.assertTrue(clazz.test);
    }

    @Test
    public void testSerialize() {
        TestClass toSerialize = new TestClass(true, true);
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
        Assertions.assertTrue(entries.containsKey("testing"));
        Assertions.assertTrue(entries.containsKey("test"));
        Assertions.assertEquals(true, entries.get("testing"));
        Assertions.assertEquals(true, entries.get("test"));
    }

    public static class ExtendingFrom {
        final boolean test;

        protected ExtendingFrom(boolean test) {
            this.test = test;
        }
    }

    public static class TestClass extends ExtendingFrom {

        private final boolean testing;

        @ConfigConstructor
        public TestClass(boolean test, boolean testing) {
            super(test);
            this.testing = testing;
        }
    }
}
