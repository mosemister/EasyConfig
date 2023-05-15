package org.easy.config.common;

import org.easy.config.Serializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class LocalDateTimeSerializer implements Serializer.KeyValue<LocalDateTime> {

    @Override
    public Map<String, Object> serialize(LocalDateTime value) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> date = CommonJavaSerializers.DATE.serialize(value.toLocalDate());
        Map<String, Object> time = CommonJavaSerializers.TIME.serialize(value.toLocalTime());
        map.putAll(date);
        map.putAll(time);
        return map;
    }

    @Override
    public LocalDateTime deserialize(Map<String, Object> type) throws Exception {
        LocalDate date = CommonJavaSerializers.DATE.deserialize(type);
        LocalTime time = CommonJavaSerializers.TIME.deserialize(type);
        return LocalDateTime.of(date, time);
    }

    @Override
    public Class<?> ofType() {
        return LocalDateTime.class;
    }
}
