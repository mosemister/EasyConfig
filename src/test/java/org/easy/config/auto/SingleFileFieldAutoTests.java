package org.easy.config.auto;

import org.easy.config.Serializer;
import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SingleFileFieldAutoTests {

    private final Serializer<File> FILE_SERIALIZER = new Serializer<>() {
        @Override
        public Map<String, Object> serialize(File value) throws Exception {
            Map<String, Object> map = new HashMap<>();
            map.put("path", value.getPath());
            return map;
        }

        @Override
        public File deserialize(Map<String, Object> map) throws Exception {
            Object path = map.get("path");
            if (path == null) {
                throw new IllegalArgumentException("Path not specified");
            }
            if (!(path instanceof String)) {
                throw new IllegalArgumentException("path is not of file");
            }
            return new File((String)path);
        }
    };

    @Test
    public void testLoad() {
        Map<String, Serializer<?>> serializers = new HashMap<>();
        serializers.put("file", FILE_SERIALIZER);
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class, serializers);
        Map<String, Object> toLoad = new HashMap<>();
        Map<String, Object> fileMap = new HashMap<>();
      
        fileMap.put("path", "testfile.txt");
        toLoad.put("fieldTest", fileMap);

        //act
        TestClass clazz;
        try {
            clazz = serializer.deserialize(toLoad);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //assert
        Assertions.assertEquals("testfile.txt", clazz.fieldTest.getName());
    }

    @Test
    public void testSerialize() {
        Map<String, Serializer<?>> serializers = new HashMap<>();
        serializers.put("file", FILE_SERIALIZER);
        TestClass toSerialize = new TestClass(new File("testfile.txt"));
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class, serializers);

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
        Assertions.assertEquals("path", innerEntry.getKey());
        Assertions.assertInstanceOf(String.class, innerEntry.getValue());
        Assertions.assertEquals("testfile.txt", innerEntry.getValue());
    }

    @Test
    public void throwExceptionOnNull() {
        AutoSerializer<TestClass> serializer = new AutoSerializer<>(TestClass.class);

        //act assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> serializer.serialize(null));
    }

    public static class TestClass {

        @ConfigField(serializer = "file")
        public final File fieldTest;

        @ConfigConstructor
        public TestClass(File fieldTest) {
            this.fieldTest = fieldTest;
        }
    }
}
