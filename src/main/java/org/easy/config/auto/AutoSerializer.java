package org.easy.config.auto;

import org.easy.config.Serializer;
import org.easy.config.auto.annotations.ConfigConstructor;
import org.easy.config.auto.annotations.ConfigField;
import org.easy.config.auto.annotations.ConfigFieldKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoSerializer<T> implements Serializer<T> {

    private final Class<?> ofType;
    private final Supplier<Map<String, Serializer<?>>> serializers;

    public AutoSerializer(Class<?> ofType) {
        this(ofType, new HashMap<>());
    }

    public AutoSerializer(Class<?> ofType, Map<String, Serializer<?>> serializer) {
        this(ofType, () -> serializer);
    }

    public AutoSerializer(Class<?> ofType, Supplier<Map<String, Serializer<?>>> serializer) {
        this.ofType = ofType;
        this.serializers = serializer;
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

        Map<String, Serializer<?>> serializers = this.mapValue(this.ofType);

        Object[] values = orderParameters(map, serializers);
        try {
            return (T) constructor.newInstance(values);
        } catch (IllegalArgumentException e) {
            String constructorParameter = Arrays.stream(constructor.getParameters()).map(p -> p.getType().getSimpleName()).collect(Collectors.joining(", "));
            String valueNames = Arrays.stream(values).map(v -> v.getClass().getSimpleName()).collect(Collectors.joining(", "));

            throw new IllegalArgumentException("Value and constructor parameters did not match.\nConstructor: " + constructorParameter + "\nValues     : " + valueNames, e);
        }
    }

    private Object[] orderParameters(Map<String, Object> map, Map<String, Serializer<?>> serializers) throws Exception {
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
            if (value instanceof Map && !fields.get(i).getType().isInstance(value)) {
                //internal deserialization
                value = deserializeField(fields.get(i), value, serializers);
            }

            ret[i] = value;
        }
        return ret;
    }

    private Object deserializeField(Field field, Object value, Map<String, Serializer<?>> serializers) throws Exception {
        if (field.isAnnotationPresent(ConfigField.class)) {
            ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
            if (configField.serializer().isEmpty()) {
                Class<?> ofType = configField.auto();
                if (configField.auto() == Object.class) {
                    ofType = field.getType();
                }
                AutoSerializer<?> auto = new AutoSerializer<>(ofType, serializers);
                return auto.deserialize((Map<String, Object>) value);
            }
            return serializers.get(configField.serializer()).deserialize((Map<String, Object>) value);

        }
        Class<?> ofType = field.getType();
        AutoSerializer<?> auto = new AutoSerializer<>(ofType, serializers);
        return auto.deserialize((Map<String, Object>) value);
    }

    private Optional<Constructor<?>> findConstructor() {
        return Arrays.stream(this.ofType.getDeclaredConstructors()).filter(cons -> cons.isAnnotationPresent(ConfigConstructor.class)).findAny();
    }

    private Map<String, Object> values(T value) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("Value should not be null");
        }
        Map<String, Object> map = new HashMap<>();
        Map<String, Serializer<?>> serializers = mapValue(value.getClass());

        for (Field field : fields(value.getClass())) {
            String name = field.getName();
            String serializerKey = null;
            ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
            if (configField != null && !configField.name().isEmpty()) {
                name = configField.name();
                serializerKey = configField.serializer();
            }
            field.setAccessible(true);
            Object obj = field.get(value);
            field.setAccessible(false);
            //serialize object
            obj = serializeMap(obj, serializerKey, serializers);
            map.put(name, obj);
        }
        return map;
    }

    private Object serializeMap(Object obj, String serializerString, Map<String, Serializer<?>> serializers) throws Exception {
        if (obj == null) {
            return obj;
        }
        Class<?> type = obj.getClass();
        if (type.isPrimitive()) {
            return obj;
        }
        if (obj instanceof String) {
            return obj;
        }
        if (obj instanceof Boolean) {
            return obj;
        }
        if (obj instanceof Integer) {
            return obj;
        }
        if (obj instanceof Double) {
            return obj;
        }
        if (obj instanceof Float) {
            return obj;
        }
        if (obj instanceof Long) {
            return obj;
        }
        if (obj instanceof Short) {
            return obj;
        }
        if (obj instanceof Byte) {
            return obj;
        }
        if (obj instanceof Character) {
            return obj;
        }
        if (serializerString != null) {
            Serializer<?> serializer = serializers.get(serializerString);
            if (serializer == null) {
                throw new IllegalStateException("Serializer for key '" + serializerString + "' cannot be found");
            }
            return serializeOther(serializerString, serializer);
        }

        //last resort
        return new AutoSerializer<>(type, serializers).serialize(obj);
    }

    private <T> Map<String, Object> serializeOther(Object obj, Serializer<T> serializer) throws Exception {
        return serializer.serialize((T) obj);
    }

    private Map<String, Serializer<?>> mapValue(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, ? extends Class<? extends Serializer<?>>> map = Arrays.stream(clazz.getDeclaredConstructors()).filter(cons -> cons.isAnnotationPresent(ConfigConstructor.class)).findAny().map(cons -> {
            ConfigConstructor anno = cons.getDeclaredAnnotation(ConfigConstructor.class);
            return Arrays.stream(anno.keys()).collect(Collectors.toMap(ConfigFieldKey::key, ConfigFieldKey::serializer));
        }).orElseGet(HashMap::new);

        Map<String, Serializer<?>> serializers = new HashMap<>();
        for (Map.Entry<String, ? extends Class<? extends Serializer<?>>> entry : map.entrySet()) {
            Constructor<? extends Serializer<?>> constructor = entry.getValue().getDeclaredConstructor();
            Serializer<?> v = constructor.newInstance();
            serializers.put(entry.getKey(), v);
        }

        for (Map.Entry<String, Serializer<?>> entry : this.serializers.get().entrySet()) {
            if (!serializers.containsKey(entry.getKey())) {
                serializers.put(entry.getKey(), entry.getValue());
            }
        }

        return serializers;
    }

    private List<Field> fields(Class<?> ofType) {
        List<Field> fields = Stream
                .of(ofType.getDeclaredFields())
                .filter(field -> {
                    if (!field.isAnnotationPresent(ConfigField.class)) {
                        return true;
                    }
                    ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
                    return !configField.exclude();
                })
                .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return Arrays.asList(ofType.getDeclaredFields());
        }
        return fields;
    }
}
