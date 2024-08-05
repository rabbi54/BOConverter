package utils.serializers;

import utils.interfaces.ByteSerialize;
import utils.interfaces.Serializer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

public class ObjectSerializer {

    private static final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
    private final Object object;

    public ObjectSerializer(Object object) {
        this.object = object;
    }

    static {
        serializers.put(Integer.class, new IntegerSerializer());
        serializers.put(UUID.class, new UUIDSerializer());
        serializers.put(String.class, new StringSerializer());
        serializers.put(Double.class, new DoubleSerializer());
    }

    public byte[] serialize() throws IllegalAccessException {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Assume 1024 bytes is enough for serialization

        for (Field field : this.object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                field.setAccessible(true);
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                if (annotation.type() == ArrayList.class) {
                    Serializer<Object> serializer = (Serializer<Object>) serializers.get(annotation.innerType());
                    AnnotationDataClass annotationDataClass = new AnnotationDataClass(
                            annotation.type(),
                            annotation.identifier(),
                            annotation.length()
                    );

                    ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
                    byte[] serializedData = arraySerializer.serialize(field.get(object), annotationDataClass);
                    buffer.put(annotation.identifier());
                    buffer.put(serializedData);
                }
                else {
                    Serializer<Object> serializer = (Serializer<Object>) serializers.get(annotation.type());
                    AnnotationDataClass annotationDataClass = new AnnotationDataClass(
                            annotation.type(),
                            annotation.identifier(),
                            annotation.length()
                    );

                    if (serializer != null) {
                        byte[] serializedData = serializer.serialize(field.get(object), annotationDataClass);
                        buffer.put(annotation.identifier());
                        buffer.put(serializedData);
                    }
                }
            }
        }
        // Resize buffer to fit the actual data size
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    public Object deserialize(byte[] data, Class<?> clazz) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Object object = clazz.getDeclaredConstructor().newInstance();

        while (buffer.hasRemaining()) {
            byte typeId = buffer.get();
            Object deserializedValue;
            ByteSerialize byteSerialize = getAnnotationFromIdentifier(typeId);
            if (byteSerialize == null) {
                return null;
            }

            int length = byteSerialize.length();
            if (length == 0) {
                length = buffer.getInt();
            }
            AnnotationDataClass annotationDataClass = new AnnotationDataClass(
                    byteSerialize.type(),
                    byteSerialize.identifier(),
                    length
            );

            if (byteSerialize.type() == ArrayList.class) {
                int size = buffer.getInt();
                Serializer<Object> serializer = (Serializer<Object>) serializers.get(byteSerialize.innerType());
                ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
                deserializedValue = arraySerializer.deserialize(getBytes(buffer, size), annotationDataClass);
            }
            else {
                Serializer<?> serializer = getSerializer(byteSerialize.type());
                deserializedValue = serializer.deserialize(getBytes(buffer, length), annotationDataClass);
            }

            Field field = findFieldByIdentifier(clazz, annotationDataClass.identifier);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, deserializedValue);
            }
        }
        return object;
    }

    public static byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private ByteSerialize getAnnotationFromIdentifier(byte type) {
        for (Field field : this.object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                field.setAccessible(true);
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                if (annotation != null && annotation.identifier() == type) {
                    return annotation;
                }
            }
        }
        return null;
    }

    private Field findFieldByIdentifier(Class<?> clazz, byte type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                if (annotation.identifier() == type) {
                    return field;
                }
            }
        }
        return null;
    }

    public static <T> Serializer<T> getSerializer(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }
        if (!serializers.containsKey(clazz)) {
            throw new IllegalArgumentException("No serializer registered for type identifier: " + clazz.getName());
        }
        Serializer<?> serializer = serializers.get(clazz);
        if (serializer == null) {
            throw new IllegalArgumentException("No serializer registered for type identifier: " + clazz.getName());
        }
        return (Serializer<T>) serializer;
    }
}

