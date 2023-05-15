package org.easy.config.common;

import org.easy.config.Serializer;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public final class CommonJavaSerializers {

    public static final FileSerializer FILE = new FileSerializer();
    public static final LocalDateSerializer DATE = new LocalDateSerializer();
    public static final LocalTimeSerializer TIME = new LocalTimeSerializer();
    public static final LocalDateTimeSerializer DATE_TIME = new LocalDateTimeSerializer();

    public static Stream<Serializer<?, ?>> serializers() {
        return Arrays
                .stream(CommonJavaSerializers.class.getDeclaredFields())
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Serializer.class.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (Serializer<?, ?>) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
