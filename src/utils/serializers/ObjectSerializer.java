package utils.serializers;

import utils.interfaces.ByteSerialize;
import utils.interfaces.Serializer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

public class ObjectSerializer {

    private static final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();

    public ObjectSerializer() {}

    static {
        serializers.put(Integer.class, new IntegerSerializer());
        serializers.put(UUID.class, new UUIDSerializer());
        serializers.put(String.class, new StringSerializer());
        serializers.put(Double.class, new DoubleSerializer());
    }

    public byte[] serialize(Object object) throws IllegalAccessException {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Assume 1024 bytes is enough for serialization

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                field.setAccessible(true);
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                serializeField(object, field, annotation, buffer);
            }
        }
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    private void serializeField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer) throws IllegalAccessException {
        if (field.get(object) == null) {
            return;
        }
        AnnotationDataClass annotationDataClass = getAnnotationDataClass(annotation);
        if (annotation.type() == ArrayList.class) {
            Serializer<Object> serializer = (Serializer<Object>) serializers.get(annotation.innerType());
            ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
            byte[] serializedData = arraySerializer.serialize(field.get(object), annotationDataClass);
            buffer.put(annotation.identifier());
            buffer.put(serializedData);
        }
        else {
            Serializer<Object> serializer = (Serializer<Object>) serializers.get(annotation.type());
            if (serializer != null) {
                byte[] serializedData = serializer.serialize(field.get(object), annotationDataClass);
                buffer.put(annotation.identifier());
                buffer.put(serializedData);
            }
        }
    }

    private AnnotationDataClass getAnnotationDataClass(ByteSerialize annotation) {
        return new AnnotationDataClass(
                annotation.type(),
                annotation.identifier(),
                annotation.length()
        );
    }

    public Object deserialize(byte[] data, Class<?> clazz) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Object object = clazz.getDeclaredConstructor().newInstance();
        Object deserializedValue;

        while (buffer.hasRemaining()) {
            byte typeId = buffer.get();
            ByteSerialize byteSerialize = getAnnotationFromIdentifier(typeId, clazz);
            if (byteSerialize == null) {
                throw new NullPointerException("No serializer found for typeId: " + typeId + " position " + buffer.position());
            }
            int length = getLength(byteSerialize, buffer);
            AnnotationDataClass annotationDataClass = getAnnotationDataClass(byteSerialize);
            deserializedValue = getObject(byteSerialize, buffer, annotationDataClass, length);
            Field field = findFieldByIdentifier(clazz, annotationDataClass.identifier);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, deserializedValue);
            }
        }
        return object;
    }

    private Object getObject(ByteSerialize byteSerialize, ByteBuffer buffer, AnnotationDataClass annotationDataClass, int length) {
        Object deserializedValue;
        if (byteSerialize.type() == ArrayList.class) {
            if (annotationDataClass.length != 0) {
                    length = buffer.getInt();
            }
            Serializer<Object> serializer = (Serializer<Object>) serializers.get(byteSerialize.innerType());
            ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
            deserializedValue = arraySerializer.deserialize(getBytes(buffer, length), annotationDataClass);
        }
        else {
            Serializer<?> serializer = getSerializer(byteSerialize.type());
            annotationDataClass.setLength(length);
            deserializedValue = serializer.deserialize(getBytes(buffer, length), annotationDataClass);
        }
        return deserializedValue;
    }

    private int getLength(ByteSerialize byteSerialize, ByteBuffer buffer) {
        int length = byteSerialize.length();
        if (length == 0) {
            length = buffer.getInt(); // for string like objects.
        }
        return length;
    }

    private byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private ByteSerialize getAnnotationFromIdentifier(byte type, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
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

