package org.easy.config.common;

import org.easy.config.Serializer;

import java.io.File;

public class FileSerializer implements Serializer.Text<File> {
    @Override
    public String serialize(File value) throws Exception {
        return value.getPath();
    }

    @Override
    public File deserialize(String type) throws Exception {
        return new File(type);
    }

    @Override
    public Class<?> ofType() {
        return File.class;
    }
}
