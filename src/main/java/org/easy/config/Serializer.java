package org.easy.config;

import java.util.Map;

public interface Serializer<T> {

    Map<String, Object> serialize(T value) throws Exception;

    T deserialize(Map<String, Object> map) throws Exception;
}
