package org.easy.config.auto;

import org.easy.config.Serializer;
import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AutoSerializer<T> implements Serializer.KeyValue<T> {

    private final Class<?> ofType;
    private final Supplier<Collection<Serializer<?, ?>>> serializers;

    public AutoSerializer(Class<?> ofType) {
        this(ofType, Collections.emptyList());
    }

    public AutoSerializer(Class<?> ofType, Collection<Serializer<?, ?>> serializer) {
        this(ofType, () -> serializer);
    }

    public AutoSerializer(Class<?> ofType, Supplier<Collection<Serializer<?, ?>>> serializer) {
        this.ofType = ofType;
        this.serializers = serializer;
    }

    @Override
    public Class<?> ofType() {
        return this.ofType;
    }

    @Override
    public Map<String, Object> serialize(T value) throws Exception {
        return values(value);
    }

    @Override
    public T deserialize(Map<String, Object> map) throws Exception {
        if (map == null) {
            throw new IllegalArgumentException("Cannot deserialize 'null'");
        }
        Optional<Constructor<?>> opConstructor = findConstructor();
        if (!opConstructor.isPresent()) {
            throw new IllegalStateException("Cannot find a constructor annotated with @ConfigConstructor in '" + this.ofType.getSimpleName() + "'");
        }
        Constructor<?> constructor = opConstructor.get();

        Collection<Serializer<?, ?>> serializers = this.mapValue(this.ofType);

        Object[] values = orderParameters(map, serializers);
        try {
            return (T) constructor.newInstance(values);
        } catch (IllegalArgumentException e) {
            String constructorParameter = Arrays.stream(constructor.getParameters()).map(p -> p.getType().getSimpleName()).collect(Collectors.joining(", "));
            String valueNames = Arrays.stream(values).map(v -> v.getClass().getSimpleName()).collect(Collectors.joining(", "));

            throw new IllegalArgumentException("Value and constructor parameters did not match.\nConstructor: " + constructorParameter + "\nValues     : " + valueNames, e);
        }
    }

    private Class<?> toPrimitive(Class<?> type) {
        if (type.isPrimitive()) {
            return type;
        }
        if (type == Boolean.class) {
            return boolean.class;
        }
        if (type == Integer.class) {
            return int.class;
        }
        if (type == Double.class) {
            return double.class;
        }
        if (type == Float.class) {
            return float.class;
        }
        if (type == Long.class) {
            return long.class;
        }
        if (type == Short.class) {
            return short.class;
        }
        if (type == Byte.class) {
            return byte.class;
        }
        if (type == Character.class) {
            return char.class;
        }
        return type;
    }

    private Object[] orderParameters(Map<String, Object> map, Collection<Serializer<?, ?>> serializers) throws Exception {
        List<Field> fields = this.fields(this.ofType);
        List<String> list = fields.stream().map(field -> {
            if (field.isAnnotationPresent(ConfigField.class)) {
                String name = field.getDeclaredAnnotation(ConfigField.class).name();
                if (!name.isEmpty()) {
                    return name;
                }
            }
            return field.getName();
        }).collect(Collectors.toList());


        Object[] ret = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            Object value = map.entrySet().stream().filter(n -> name.equalsIgnoreCase(n.getKey())).findAny().map(Map.Entry::getValue).orElseThrow(() -> new IllegalArgumentException("Cannot find a parameter with the name of '" + name + "'"));
            Class<?> type = fields.get(i).getType();
            //type = this.toPrimitive(type);
            Class<?> valueType = value.getClass();
            boolean isInstance = (type.isInstance(value) || this.toPrimitive(type).isInstance(value));
            boolean isEqualType = this.toPrimitive(type).equals(this.toPrimitive(valueType));
            if (!isInstance && !isEqualType) {
                //internal deserialization
                value = deserializeField(fields.get(i), value, serializers);
            }

            ret[i] = value;
        }
        return ret;
    }

    private <T> Object deserialize(Object value, Serializer<?, T> serializer) throws Exception {
        T casted = (T) value;
        return serializer.deserialize(casted);
    }

    private Object deserializeField(Field field, Object value, Collection<Serializer<?, ?>> serializers) throws Exception {
        if (!field.isAnnotationPresent(ConfigField.class)) {
            return deserializeType(field.getType(), value, serializers);
        }
        ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
        if (!configField.serializer().isInterface()) {
            Serializer<?, ?> serializer = configField.serializer().getDeclaredConstructor().newInstance();
            return deserialize(value, serializer);
        }
        if (configField.auto() != Object.class) {
            AutoSerializer<?> serializer = new AutoSerializer<>(configField.auto());
            return deserialize(value, serializer);
        }
        return deserializeType(field.getType(), value, serializers);
    }

    private boolean isAutoSerializable(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors()).anyMatch(cons -> cons.isAnnotationPresent(ConfigConstructor.class));
    }

    private Object deserializeType(Class<?> type, Object value, Collection<Serializer<?, ?>> serializers) throws Exception {
        if (isAutoSerializable(type)) {
            return deserialize(value, new AutoSerializer<>(type));
        }
        Serializer<?, ?> serializer = serializers.stream().filter(s -> s.ofType().isAssignableFrom(type)).findFirst().orElseThrow(() -> new IllegalStateException("Cannot find serializer for " + type.getSimpleName()));
        return deserialize(value, serializer);
    }

    private <T> Object serializeType(Class<?> type, T value, Collection<Serializer<?, ?>> serializers) throws Exception {
        Serializer<?, ?> serializer = serializers.stream().filter(s -> s.ofType().isAssignableFrom(type)).findFirst().orElseThrow(() -> new IllegalStateException("Cannot find serializer for " + type.getSimpleName()));
        return ((Serializer<T, ?>) serializer).serialize(value);
    }

    private Optional<Constructor<?>> findConstructor() {
        return Arrays.stream(this.ofType.getDeclaredConstructors()).filter(cons -> cons.isAnnotationPresent(ConfigConstructor.class)).findAny();
    }

    private Map<String, Object> values(T value) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("Value should not be null");
        }
        Map<String, Object> map = new HashMap<>();
        Collection<Serializer<?, ?>> serializers = mapValue(value.getClass());

        for (Field field : fields(value.getClass())) {
            String name = field.getName();
            Serializer<?, ?> serializerKey = null;
            ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
            if (configField != null && !configField.name().isEmpty()) {
                name = configField.name();
            }
            if (configField != null && !configField.serializer().isInterface()) {
                serializerKey = configField.serializer().getDeclaredConstructor().newInstance();
            }
            Object obj;

            try {
                field.setAccessible(true);
                obj = field.get(value);
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access fields of " + field.getType().getSimpleName() + "(field inside " + value.getClass().getSimpleName() + ")" + " due to Java9 blocking. Create a manual serializer for the type of " + field.getType().getSimpleName(), e);
            }
            //serialize object
            obj = serializeMap(obj, serializerKey, serializers);
            map.put(name, obj);
        }
        return map;
    }

    private Object serializeMap(Object obj, Serializer<?, ?> serializerToUse, Collection<Serializer<?, ?>> serializers) throws Exception {
        if (obj == null) {
            return obj;
        }
        Class<?> type = this.toPrimitive(obj.getClass());
        if (type.isPrimitive()) {
            return obj;
        }
        if (obj instanceof String) {
            return obj;
        }
        if (serializerToUse != null) {
            return serializeOther(obj, serializerToUse);
        }
        if (this.isAutoSerializable(type)) {
            return new AutoSerializer<>(type, serializers).serialize(obj);
        }
        return this.serializeType(type, obj, serializers);
    }

    private <T> Object serializeOther(Object obj, Serializer<T, ?> serializer) throws Exception {
        return serializer.serialize((T) obj);
    }

    private Collection<Serializer<?, ?>> mapValue(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Collection<Class<Serializer<?, ?>>> map = Arrays.stream(clazz.getDeclaredConstructors()).filter(cons -> cons.isAnnotationPresent(ConfigConstructor.class)).findAny().map(cons -> {
            ConfigConstructor anno = cons.getDeclaredAnnotation(ConfigConstructor.class);
            return Arrays.stream(anno.serializers()).map(t -> (Class<Serializer<?, ?>>) t).collect(Collectors.toList());
        }).orElseGet(ArrayList::new);

        Collection<Serializer<?, ?>> serializers = new HashSet<>();
        for (Class<? extends Serializer<?, ?>> entry : map) {
            if (entry.isInterface()) {
                continue;
            }
            Constructor<? extends Serializer<?, ?>> constructor = entry.getDeclaredConstructor();
            Serializer<?, ?> v = constructor.newInstance();
            serializers.add(v);
        }
        serializers.addAll(this.serializers.get());
        return serializers;
    }

    private List<Field> allFields(Class<?> ofType) {
        List<Field> fields = new LinkedList<>();
        Class<?> targetType = ofType;
        while (targetType != null) {
            fields.addAll(Arrays.asList(targetType.getDeclaredFields()));
            targetType = targetType.getSuperclass();
        }
        return fields;
    }

    private List<Field> fields(Class<?> ofType) {
        return allFields(ofType).stream()
                .filter(field -> {
                    if (!field.isAnnotationPresent(ConfigField.class)) {
                        return true;
                    }
                    ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
                    return !configField.exclude();
                })
                .collect(Collectors.toList());
    }
}
