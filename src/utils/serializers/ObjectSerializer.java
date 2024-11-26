package utils.serializers;

import models.SleepBinning;
import org.jetbrains.annotations.NotNull;
import utils.dataclass.AnnotationDataClass;
import utils.dataclass.SerializationParameter;
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
        if (object == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024); // Assume 1024 bytes is enough for serialization

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ByteSerialize.class)) {
                field.setAccessible(true);
                ByteSerialize annotation = field.getAnnotation(ByteSerialize.class);
                try {
                    SerializationParameter builder = new SerializationParameter.Builder()
                            .byteSerialize(annotation)
                            .buffer(buffer)
                            .field(field)
                            .object(object)
                            .build();
                    serializeField(builder);
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
                throw new NullPointerException("Class: " + clazz.getName() + " No serializer found for typeId: " + typeId + " position " + buffer.position());
            }
            SerializationParameter builder = new SerializationParameter.Builder()
                    .byteSerialize(byteSerialize)
                    .buffer(buffer)
                    .clazz(clazz)
                    .object(object)
                    .typeId(typeId)
                    .build();
            addFieldValue(builder);
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


    private void serializeField(SerializationParameter parameterBuilder) throws Exception {

        Object fieldValue = getFieldValue(parameterBuilder.field(), parameterBuilder.object());
        if (fieldValue == null && !parameterBuilder.byteSerialize().required()) {
            return;
        }

        AnnotationDataClass annotationDataClass = getAnnotationDataClass(parameterBuilder.byteSerialize());
        checkSupportedSerializer(annotationDataClass.type);

        SerializationParameter builder = new SerializationParameter.Builder()
                .from(parameterBuilder)
                .annotationDataClass(annotationDataClass)
                .build();

        if (annotationDataClass.type == Object.class) {
            serializeNestedField(builder);
        }
        else if (parameterBuilder.byteSerialize().type() == ArraySerializer.class) {
            serializeArrayField(builder);
        } else {
            serializeSimpleField(builder);
        }
    }

    private void serializeNestedField(SerializationParameter parameterBuilder) throws Exception {
        Object fieldValue = getFieldValue(parameterBuilder.field(), parameterBuilder.object());
        byte[] nestedSerializedData = serialize(fieldValue);
        if (nestedSerializedData == null) {
            return;
        }
        parameterBuilder.buffer().put(parameterBuilder.byteSerialize().identifier());
        IntegerSerializer.putInt(parameterBuilder.buffer(), nestedSerializedData.length);
        parameterBuilder.buffer().put(nestedSerializedData);
    }

    private void serializeArrayField(SerializationParameter builder) throws Exception {
        Serializer<Object> innerSerializer = getSerializerForArrayField(builder.byteSerialize(), builder.field(), builder.annotationDataClass());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);

        Object fieldValue = getFieldValue(builder.field(), builder.object());
        @SuppressWarnings("unchecked")
        byte[] serializedData = arraySerializer.serialize((ArrayList<Object>) fieldValue, builder.annotationDataClass());
        if (serializedData != null) {
            builder.buffer().put(builder.byteSerialize().identifier());
            builder.buffer().put(serializedData);
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

    private void serializeSimpleField(SerializationParameter builder) throws Exception {
        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(builder.byteSerialize().type());

        checkSerializerFieldCompatibility(serializer.getClass(), builder.field().getType());
        Object fieldValue = getFieldValue(builder.field(), builder.object());
        byte[] serializedData = serializer.serialize(fieldValue, builder.annotationDataClass());
        if (serializedData == null) {
            return;
        }
        builder.buffer().put(builder.byteSerialize().identifier());
        builder.buffer().put(serializedData);
    }

    private void addFieldValue(SerializationParameter parameterBuilder) throws Exception {
        int length = getLength(parameterBuilder.byteSerialize(), parameterBuilder.buffer());
        AnnotationDataClass annotationDataClass = getAnnotationDataClass(parameterBuilder.byteSerialize());
        checkSupportedSerializer(annotationDataClass.type);
        Field field = findFieldByIdentifier(parameterBuilder.clazz(), annotationDataClass.identifier);
        Object deserializedValue;

        if (field == null) {
            throw new UnsupportedOperationException(parameterBuilder.clazz().getName() + " has no field for typeId: " + parameterBuilder.typeId() + " position " + parameterBuilder.buffer().position());
        }

        SerializationParameter builder = new SerializationParameter.Builder()
                .from(parameterBuilder)
                .field(field)
                .annotationDataClass(annotationDataClass)
                .length(length)
                .build();
        deserializedValue = getDeserializedValue(builder);
        setFieldValue(parameterBuilder, field, deserializedValue);
    }

    private void setFieldValue(SerializationParameter parameterBuilder, Field field, Object deserializedValue) throws IllegalAccessException {
        field.setAccessible(true);
        Method setterMethod = null;
        try {
            setterMethod = getFieldSetterMethod(field);
            setterMethod.invoke(parameterBuilder.object(), deserializedValue);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            field.set(parameterBuilder.object(), deserializedValue);
        }
    }

    private Object getFieldValue(Field field, Object object) {
        try {
            Method getterMethod = getFieldGetterMethod(field);
            return getterMethod.invoke(object);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return getFieldValueDirectly(field, object);
        }
    }

    private Object getFieldValueDirectly(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Object getDeserializedValue(SerializationParameter parameterBuilder) throws Exception {
        Object deserializedValue;
        if (parameterBuilder.annotationDataClass().type == Object.class) {
            byte[] nestedData = getBytes(parameterBuilder.buffer(), parameterBuilder.length());
            deserializedValue = deserialize(nestedData, parameterBuilder.field().getType());
        }
        else {
            checkSerializerFieldCompatibility(parameterBuilder.annotationDataClass().type, parameterBuilder.field().getType());
            deserializedValue = getObject(parameterBuilder);
        }
        return deserializedValue;
    }

    private Object getObject(SerializationParameter parameterBuilder) throws SerializerCreationException {
        try {
            if (parameterBuilder.byteSerialize().type() == ArraySerializer.class) {
                return deserializeArrayField(parameterBuilder);
            } else {
                return deserializeField(parameterBuilder);
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

    private Object deserializeArrayField(SerializationParameter parameterBuilder) throws Exception {
        if (parameterBuilder.byteSerialize().innerType() == Object.class) {
            Class<?> innerClass = getInnerClass(parameterBuilder.field());
            parameterBuilder.annotationDataClass().setType(innerClass);
            byte[] nestedData = getBytes(parameterBuilder.buffer(), parameterBuilder.length());
            return new ArraySerializer<>(this).deserialize(nestedData, parameterBuilder.annotationDataClass());
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(parameterBuilder.byteSerialize().innerType());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
        return arraySerializer.deserialize(getBytes(parameterBuilder.buffer(), parameterBuilder.length()), parameterBuilder.annotationDataClass());
    }

    private Object deserializeField(SerializationParameter parameterBuilder) throws Exception {
        AnnotationDataClass annotationDataClass = parameterBuilder.annotationDataClass();
        annotationDataClass.setLength(parameterBuilder.length());
        Serializer<?> serializer = getSerializer(parameterBuilder.byteSerialize().type());
        return serializer.deserialize(getBytes(parameterBuilder.buffer(), parameterBuilder.length()), annotationDataClass);
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
