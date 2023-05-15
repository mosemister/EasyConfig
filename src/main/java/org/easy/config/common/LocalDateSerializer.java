package org.easy.config.common;

import org.easy.config.Serializer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LocalDateSerializer implements Serializer.KeyValue<LocalDate> {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day-of-month";

    @Override
    public Map<String, Object> serialize(LocalDate value) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(YEAR, value.getYear());
        map.put(MONTH, value.getMonthValue());
        map.put(DAY, value.getDayOfMonth());
        return map;
    }

    @Override
    public LocalDate deserialize(Map<String, Object> type) throws Exception {
        int year = this.isValid(type.get(YEAR), YEAR);
        int month = this.isValid(type.get(MONTH), MONTH);
        int day = this.isValid(type.get(DAY), DAY);
        if (day > 31) {
            throw new IllegalArgumentException(DAY + " cannot be greater then 31");
        }
        if (month > 12) {
            throw new IllegalArgumentException(MONTH + " cannot be greater then 12");
        }
        return LocalDate.of(year, month, day);
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
        return LocalDate.class;
    }
}
