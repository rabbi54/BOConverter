package utils.serializers;

import org.jetbrains.annotations.NotNull;
import utils.exceptions.SerializerMismatchException;
import utils.interfaces.ByteSerialize;
import utils.interfaces.Serializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

public class ObjectSerializer {

    private static final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
    private static final Map<Class<?>, Class<?>> serializerFieldCompatibilityMap = new HashMap<>();

    public ObjectSerializer() {}

    static {
        serializers.put(IntegerSerializer.class, new IntegerSerializer());
        serializers.put(UUIDSerializer.class, new UUIDSerializer());
        serializers.put(StringSerializer.class, new StringSerializer());
        serializers.put(DoubleSerializer.class, new DoubleSerializer());
    }

    static {
        serializerFieldCompatibilityMap.put(IntegerSerializer.class, Integer.class);
        serializerFieldCompatibilityMap.put(UUIDSerializer.class, UUID.class);
        serializerFieldCompatibilityMap.put(StringSerializer.class, String.class);
        serializerFieldCompatibilityMap.put(DoubleSerializer.class, Double.class);
        serializerFieldCompatibilityMap.put(ArraySerializer.class, ArrayList.class);
    }

    public byte[] serialize(Object object) throws Exception {
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

    private void serializeField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer) throws Exception {
        if (field.get(object) == null) {
            return;
        }
        AnnotationDataClass annotationDataClass = getAnnotationDataClass(annotation);
        if (annotation.type() == ArraySerializer.class) {
            @SuppressWarnings("unchecked")
            Serializer<Object> innerSerializer = (Serializer<Object>) serializers.get(annotation.innerType());

            Class<?> innerClass = getInnerClass(field);
            checkSerializerFieldCompatibility(innerSerializer.getClass(), innerClass);
            ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);
            byte[] serializedData = arraySerializer.serialize(field.get(object), annotationDataClass);
            buffer.put(annotation.identifier());
            buffer.put(serializedData);
        }
        else {
            @SuppressWarnings("unchecked")
            Serializer<Object> serializer = (Serializer<Object>) serializers.get(annotation.type());
            if (serializer != null) {
                checkSerializerFieldCompatibility(serializer.getClass(), field.getType());
                byte[] serializedData = serializer.serialize(field.get(object), annotationDataClass);
                buffer.put(annotation.identifier());
                buffer.put(serializedData);
            }
        }
    }

    private Class<?> getInnerClass(Field field) throws Exception {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            return (Class<?>) typeArguments[0];
        }
        throw new Exception(field.getName() + " is not a parameterized type");
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
            Field field = findFieldByIdentifier(clazz, annotationDataClass.identifier);
            if (field != null) {
                checkSerializerFieldCompatibility(annotationDataClass.getType(), field.getType());
                deserializedValue = getObject(byteSerialize, buffer, annotationDataClass, length);
                field.setAccessible(true);
                field.set(object, deserializedValue);
            } else {
                throw new UnsupportedOperationException(clazz.getName() + "has no field for typeId: " + typeId + " position " + buffer.position());
            }
        }
        return object;
    }

    private Object getObject(ByteSerialize byteSerialize, ByteBuffer buffer, AnnotationDataClass annotationDataClass, int length) {
        Object deserializedValue;
        if (byteSerialize.type() == ArraySerializer.class) {
            if (annotationDataClass.length != 0) {
                    length = buffer.getInt();
            }
            @SuppressWarnings("unchecked")
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

    private static void checkSerializerFieldCompatibility(@NotNull Class<?> serializerClass, @NotNull Class<?> fieldClass) throws SerializerMismatchException {
        Class<?> compatibleClass = serializerFieldCompatibilityMap.get(serializerClass);
        if (compatibleClass == null) {
            throw new SerializerMismatchException( serializerClass.getName() + " serializer has no corresponding compatibility for class " + fieldClass.getName());
        }
        if (compatibleClass != fieldClass) {
            throw new SerializerMismatchException( serializerClass.getName() + " has no compatibility for class " + fieldClass.getName());
        }

    }
}

