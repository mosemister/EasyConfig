package org.easy.config;

import java.util.Map;

public interface Serializer<T, E> {

    E serialize(T value) throws Exception;

    T deserialize(E type) throws Exception;

    Class<?> ofType();

    interface KeyValue<T> extends Serializer<T, Map<String, Object>> {

    }

    interface Text<T> extends Serializer<T, String> {

    }
}
