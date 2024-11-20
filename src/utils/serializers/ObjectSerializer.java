package utils.serializers;

import models.SleepBinning;
import org.jetbrains.annotations.NotNull;
import utils.dataclass.AnnotationDataClass;
import utils.exceptions.SerializerCreationException;
import utils.exceptions.SerializerMismatchException;
import utils.interfaces.ByteSerialize;
import utils.interfaces.Serializer;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

public class ObjectSerializer implements Serializer<Object> {

    private static final Map<Class<?>, Class<?>> serializerFieldCompatibilityMap = new HashMap<>();

    public ObjectSerializer() {}

    static {
        serializerFieldCompatibilityMap.put(IntegerSerializer.class, Integer.class);
        serializerFieldCompatibilityMap.put(UUIDSerializer.class, String.class);
        serializerFieldCompatibilityMap.put(StringSerializer.class, String.class);
        serializerFieldCompatibilityMap.put(DoubleSerializer.class, Double.class);
        serializerFieldCompatibilityMap.put(ArraySerializer.class, ArrayList.class);
        serializerFieldCompatibilityMap.put(BooleanSerializer.class, Boolean.class);
        serializerFieldCompatibilityMap.put(ByteIntSerializer.class, Integer.class);
        serializerFieldCompatibilityMap.put(ShortSerializer.class, Short.class);
        serializerFieldCompatibilityMap.put(FloatSerializer.class, Float.class);
        serializerFieldCompatibilityMap.put(LongSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(LongFrom4ByteSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(LocationDataSerializer.class, Double.class);
        serializerFieldCompatibilityMap.put(TimeSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(SleepBinningSerializer.class, SleepBinning.class);

    }

    public byte[] serialize(Object object) {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Assume 1024 bytes is enough for serialization

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                field.setAccessible(true);
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                try {
                    serializeField(object, field, annotation, buffer);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    public Object deserialize(byte[] bytes, AnnotationDataClass annotationDataClass) {
        try {
            return deserialize(bytes, annotationDataClass.type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    public Object deserialize(byte[] data, Class<?> clazz) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Object object = clazz.getDeclaredConstructor().newInstance();

        while (buffer.hasRemaining()) {
            byte typeId = buffer.get();
            ByteSerialize byteSerialize = getAnnotationFromIdentifier(typeId, clazz);
            if (byteSerialize == null) {
                throw new NullPointerException("No serializer found for typeId: " + typeId + " position " + buffer.position());
            }
            addFieldValue(clazz, byteSerialize, buffer, object, typeId);
        }
        return object;
    }

    private static <T> Serializer<T> getSerializer(Class<T> clazz) throws SerializerCreationException {
        if (clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }

        try {
            return (Serializer<T>) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new SerializerCreationException("Failed to create serializer for class: " + clazz.getName(), e);
        }
    }


    private void serializeField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer) throws Exception {

        Method getterMethod = getFieldGetterMethod(field);
        if (getterMethod.invoke(object) == null && !annotation.required()) {
            return;
        }

        AnnotationDataClass annotationDataClass = getAnnotationDataClass(annotation);
        checkSupportedSerializer(annotationDataClass.type);

        if (annotationDataClass.type == Object.class) {
            serializeNestedField(object, field, annotation, buffer);
        }
        else if (annotation.type() == ArraySerializer.class) {
            serializeArrayField(object, field, annotation, buffer, annotationDataClass);
        } else {
            serializeSimpleField(object, field, annotation, buffer, annotationDataClass);
        }
    }

    private void serializeNestedField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer) throws Exception {
        Method getterMethod = getFieldGetterMethod(field);
        byte[] nestedSerializedData = serialize(getterMethod.invoke(object));
        if (nestedSerializedData == null) {
            return;
        }
        buffer.put(annotation.identifier());
        IntegerSerializer.putInt(buffer, nestedSerializedData.length);
        buffer.put(nestedSerializedData);
    }

    private void serializeArrayField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer, AnnotationDataClass annotationDataClass) throws Exception {
        Serializer<Object> innerSerializer = getSerializerForArrayField(annotation, field, annotationDataClass);
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);

        Method getterMethod = getFieldGetterMethod(field);
        @SuppressWarnings("unchecked")
        byte[] serializedData = arraySerializer.serialize((ArrayList<Object>) getterMethod.invoke(object), annotationDataClass);
        if (serializedData != null) {
            buffer.put(annotation.identifier());
            buffer.put(serializedData);
        }
    }

    private Serializer<Object> getSerializerForArrayField(ByteSerialize annotation, Field field, AnnotationDataClass annotationDataClass) throws Exception {
        if (annotation.innerType() == Object.class) {
            annotationDataClass.setType(Object.class);
            return new ObjectSerializer();
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> innerSerializer = (Serializer<Object>) getSerializer(annotation.innerType());
        Class<?> innerClass = getInnerClass(field);
        checkSerializerFieldCompatibility(innerSerializer.getClass(), innerClass);
        return innerSerializer;
    }

    private void serializeSimpleField(Object object, Field field, ByteSerialize annotation, ByteBuffer buffer, AnnotationDataClass annotationDataClass) throws Exception {
        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(annotation.type());

        checkSerializerFieldCompatibility(serializer.getClass(), field.getType());
        Method getterMethod = getFieldGetterMethod(field);
        byte[] serializedData = serializer.serialize(getterMethod.invoke(object), annotationDataClass);
        if (serializedData == null) {
            return;
        }
        buffer.put(annotation.identifier());
        buffer.put(serializedData);
    }

    private void addFieldValue(Class<?> clazz, ByteSerialize byteSerialize, ByteBuffer buffer, Object object, byte typeId) throws Exception {
        int length = getLength(byteSerialize, buffer);
        AnnotationDataClass annotationDataClass = getAnnotationDataClass(byteSerialize);
        checkSupportedSerializer(annotationDataClass.type);
        Field field = findFieldByIdentifier(clazz, annotationDataClass.identifier);
        Object deserializedValue;
        if (field != null) {
            if (annotationDataClass.type == Object.class) {
                byte[] nestedData = getBytes(buffer, length);
                deserializedValue = deserialize(nestedData, field.getType());
            }
            else {
                checkSerializerFieldCompatibility(annotationDataClass.type, field.getType());
                deserializedValue = getObject(byteSerialize, buffer, annotationDataClass, length, field);
            }
            field.setAccessible(true);
            Method setterMethod = getFieldSetterMethod(field);
            setterMethod.invoke(object, deserializedValue);
        } else {
            throw new UnsupportedOperationException(clazz.getName() + " has no field for typeId: " + typeId + " position " + buffer.position());
        }
    }

    private Object getObject(ByteSerialize byteSerialize, ByteBuffer buffer, AnnotationDataClass annotationDataClass, int length, Field field) throws SerializerCreationException {
        try {
            if (byteSerialize.type() == ArraySerializer.class) {
                return deserializeArrayField(byteSerialize, buffer, annotationDataClass, length, field);
            } else {
                return deserializeField(byteSerialize, buffer, annotationDataClass, length);
            }
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    private Method getFieldGetterMethod(Field field) throws NoSuchMethodException {
        String fieldName = field.getName();
        String getterMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return field.getDeclaringClass().getMethod(getterMethodName);
    }

    private Method getFieldSetterMethod(Field field) throws NoSuchMethodException {
        String fieldName = field.getName();
        String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return field.getDeclaringClass().getMethod(setterMethodName, field.getType());
    }

    private Object deserializeArrayField(ByteSerialize byteSerialize, ByteBuffer buffer, AnnotationDataClass annotationDataClass, int length, Field field) throws Exception {
        if (byteSerialize.innerType() == Object.class) {
            Class<?> innerClass = getInnerClass(field);
            annotationDataClass.setType(innerClass);
            byte[] nestedData = getBytes(buffer, length);
            return new ArraySerializer<>(this).deserialize(nestedData, annotationDataClass);
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(byteSerialize.innerType());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
        return arraySerializer.deserialize(getBytes(buffer, length), annotationDataClass);
    }

    private Object deserializeField(ByteSerialize byteSerialize, ByteBuffer buffer, AnnotationDataClass annotationDataClass, int length) throws Exception {
        annotationDataClass.setLength(length);
        Serializer<?> serializer = getSerializer(byteSerialize.type());
        return serializer.deserialize(getBytes(buffer, length), annotationDataClass);
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
                annotation.length(),
                annotation.required()
        );
    }

    private int getLength(ByteSerialize byteSerialize, ByteBuffer buffer) {
        int length = byteSerialize.length();
        if (length == 0 || byteSerialize.type() == ArraySerializer.class) { // contains variable length objects
            length = IntegerSerializer.getInt(buffer); // for string reads size of the strings, for array reads total bytes in the array
        }
        return length;
    }

    private byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private static void checkSerializerFieldCompatibility(@NotNull Class<?> serializerClass, @NotNull Class<?> fieldClass) throws SerializerMismatchException {
        Class<?> compatibleClass = serializerFieldCompatibilityMap.get(serializerClass);
        if (compatibleClass == null) {
            throw new SerializerMismatchException(serializerClass.getName() + " serializer has no corresponding compatibility for class " + fieldClass.getName());
        }
        if (compatibleClass != fieldClass) {
            throw new SerializerMismatchException(serializerClass.getName() + " has no compatibility for class " + fieldClass.getName());
        }
    }

    private static void checkSupportedSerializer(@NotNull Class<?> clazz) {
        if (clazz == Object.class ) {
            return;
        }
        if (!serializerFieldCompatibilityMap.containsKey(clazz)) {
            throw new IllegalArgumentException("No serializer registered for type : " + clazz.getName());
        }
    }
}
