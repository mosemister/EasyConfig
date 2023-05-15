package org.easy.config.common;

import org.easy.config.Serializer;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class LocalTimeSerializer implements Serializer.KeyValue<LocalTime> {

    private static final String HOURS = "hours";
    private static final String MINUTES = "minutes";
    private static final String SECONDS = "seconds";
    private static final String NANO = "nano";

    @Override
    public Map<String, Object> serialize(LocalTime value) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(HOURS, value.getHour());
        map.put(MINUTES, value.getMinute());
        map.put(SECONDS, value.getSecond());
        map.put(NANO, value.getNano());
        return map;
    }

    @Override
    public LocalTime deserialize(Map<String, Object> type) throws Exception {
        int hours = this.isValid(type.get(HOURS), HOURS);
        int min = this.isValid(type.get(MINUTES), MINUTES);
        int sec = this.isValid(type.get(SECONDS), SECONDS);
        int nano = this.isValid(type.get(NANO), NANO);
        return LocalTime.of(hours, min, sec, nano);
    }

    private int isValid(Object obj, String key) {
        if (obj == null) {
            throw new IllegalArgumentException(key + " is not specified");
        }
        if (!(obj instanceof Integer)) {
            throw new IllegalArgumentException(key + " can only be a whole number");
        }
        int value = (int) obj;
        if (value < 0) {
            throw new IllegalArgumentException(key + " must be zero or greater");
        }
        return value;
    }

    @Override
    public Class<?> ofType() {
        return LocalTime.class;
    }
}
