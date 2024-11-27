package serialization.serializers;

import serialization.dataclass.SerializedFieldAttributes;
import serialization.dataclass.SerializationParameter;
import serialization.exceptions.SerializerCreationException;
import serialization.interfaces.SerializedField;
import serialization.interfaces.Serializer;
import serialization.managers.SerializedFieldManager;
import serialization.managers.ReflectionManager;
import serialization.validators.SerializationCompatibilityValidator;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

import static serialization.SerializationConstants.MAX_BUFFER_SIZE;

public class ObjectSerializer implements Serializer<Object> {

    private final ReflectionManager reflectionManager = new ReflectionManager();
    private final SerializedFieldManager serializedFieldManager = new SerializedFieldManager();

    public ObjectSerializer() {
    }

    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);

        for (Field field : object.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedField.class)) {
                continue;
            }
            field.setAccessible(true);
            SerializedField annotation = field.getAnnotation(SerializedField.class);
            try {
                SerializationParameter builder = new SerializationParameter.Builder()
                        .serializedField(annotation)
                        .buffer(buffer)
                        .field(field)
                        .object(object)
                        .build();
                serializeField(builder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    private static <T> Serializer<T> getSerializer(Class<T> clazz) throws SerializerCreationException {
        if (clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }

        try {
            return (Serializer<T>) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new SerializerCreationException("Failed to create serializer for class: " + clazz.getName(), e);
        }
    }

    private void serializeField(SerializationParameter parameterBuilder) throws Exception {

        Object fieldValue = reflectionManager.getFieldValue(parameterBuilder.field(), parameterBuilder.object());
        if (fieldValue == null && !parameterBuilder.serializedField().required()) {
            return;
        }

        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(parameterBuilder.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);

        SerializationParameter builder = new SerializationParameter.Builder()
                .from(parameterBuilder)
                .annotationDataClass(serializedFieldAttributes)
                .build();

        if (serializedFieldAttributes.type == Object.class) {
            serializeNestedField(builder);
        } else if (parameterBuilder.serializedField().type() == ArraySerializer.class) {
            serializeArrayField(builder);
        } else {
            serializeSimpleField(builder);
        }
    }

    private void serializeNestedField(SerializationParameter parameterBuilder) {
        Object fieldValue = reflectionManager.getFieldValue(parameterBuilder.field(), parameterBuilder.object());
        byte[] nestedSerializedData = serialize(fieldValue);
        if (nestedSerializedData == null) {
            return;
        }
        parameterBuilder.buffer().put(parameterBuilder.serializedField().identifier());
        IntegerSerializer.putInt(parameterBuilder.buffer(), nestedSerializedData.length);
        parameterBuilder.buffer().put(nestedSerializedData);
    }

    private void serializeArrayField(SerializationParameter builder) throws Exception {
        Serializer<Object> innerSerializer = getSerializerForArrayField(builder.serializedField(), builder.field(), builder.serializedFieldAttributes());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);

        Object fieldValue = reflectionManager.getFieldValue(builder.field(), builder.object());
        @SuppressWarnings("unchecked")
        byte[] serializedData = arraySerializer.serialize((ArrayList<Object>) fieldValue, builder.serializedFieldAttributes());
        if (serializedData != null) {
            builder.buffer().put(builder.serializedField().identifier());
            builder.buffer().put(serializedData);
        }
    }

    private Serializer<Object> getSerializerForArrayField(SerializedField annotation, Field field, SerializedFieldAttributes serializedFieldAttributes) throws Exception {
        if (annotation.innerType() == Object.class) {
            serializedFieldAttributes.setType(Object.class);
            return new ObjectSerializer();
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> innerSerializer = (Serializer<Object>) getSerializer(annotation.innerType());
        Class<?> innerClass = serializedFieldManager.getInnerClass(field);
        SerializationCompatibilityValidator.checkSerializerFieldCompatibility(innerSerializer.getClass(), innerClass);
        return innerSerializer;
    }

    private void serializeSimpleField(SerializationParameter builder) throws Exception {
        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(builder.serializedField().type());

        SerializationCompatibilityValidator.checkSerializerFieldCompatibility(serializer.getClass(), builder.field().getType());
        Object fieldValue = reflectionManager.getFieldValue(builder.field(), builder.object());
        byte[] serializedData = serializer.serialize(fieldValue, builder.serializedFieldAttributes());
        if (serializedData == null) {
            return;
        }
        builder.buffer().put(builder.serializedField().identifier());
        builder.buffer().put(serializedData);
    }

    public Object deserialize(byte[] bytes, SerializedFieldAttributes serializedFieldAttributes) {
        try {
            return deserialize(bytes, serializedFieldAttributes.type);
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
            SerializedField serializedField = serializedFieldManager.getSerializedFieldByIdentifier(typeId, clazz);
            if (serializedField == null) {
                throw new NullPointerException("Class: " + clazz.getName() + " No serializer found for typeId: " + typeId + " position " + buffer.position());
            }
            SerializationParameter builder = new SerializationParameter.Builder()
                    .serializedField(serializedField)
                    .buffer(buffer)
                    .clazz(clazz)
                    .object(object)
                    .typeId(typeId)
                    .build();
            addFieldValue(builder);
        }
        return object;
    }

    private void addFieldValue(SerializationParameter parameterBuilder) throws Exception {
        int length = getLength(parameterBuilder.serializedField(), parameterBuilder.buffer());
        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(parameterBuilder.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);
        Field field = serializedFieldManager.findFieldByIdentifier(parameterBuilder.clazz(), serializedFieldAttributes.identifier);
        Object deserializedValue;

        if (field == null) {
            throw new UnsupportedOperationException(parameterBuilder.clazz().getName() + " has no field for typeId: " + parameterBuilder.typeId() + " position " + parameterBuilder.buffer().position());
        }

        SerializationParameter builder = new SerializationParameter.Builder()
                .from(parameterBuilder)
                .field(field)
                .annotationDataClass(serializedFieldAttributes)
                .length(length)
                .build();
        deserializedValue = getDeserializedValue(builder);
        reflectionManager.setFieldValue(parameterBuilder, field, deserializedValue);
    }

    private Object getDeserializedValue(SerializationParameter parameterBuilder) throws Exception {
        Object deserializedValue;
        if (parameterBuilder.serializedFieldAttributes().type == Object.class) {
            byte[] nestedData = getBytes(parameterBuilder.buffer(), parameterBuilder.length());
            deserializedValue = deserialize(nestedData, parameterBuilder.field().getType());
        } else {
            SerializationCompatibilityValidator.checkSerializerFieldCompatibility(parameterBuilder.serializedFieldAttributes().type, parameterBuilder.field().getType());
            deserializedValue = getObject(parameterBuilder);
        }
        return deserializedValue;
    }

    private Object getObject(SerializationParameter parameterBuilder) {
        try {
            if (parameterBuilder.serializedField().type() == ArraySerializer.class) {
                return deserializeArrayField(parameterBuilder);
            } else {
                return deserializeField(parameterBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    private Object deserializeArrayField(SerializationParameter parameterBuilder) throws Exception {
        if (parameterBuilder.serializedField().innerType() == Object.class) {
            Class<?> innerClass = serializedFieldManager.getInnerClass(parameterBuilder.field());
            parameterBuilder.serializedFieldAttributes().setType(innerClass);
            byte[] nestedData = getBytes(parameterBuilder.buffer(), parameterBuilder.length());
            return new ArraySerializer<>(this).deserialize(nestedData, parameterBuilder.serializedFieldAttributes());
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(parameterBuilder.serializedField().innerType());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
        return arraySerializer.deserialize(getBytes(parameterBuilder.buffer(), parameterBuilder.length()), parameterBuilder.serializedFieldAttributes());
    }

    private Object deserializeField(SerializationParameter parameterBuilder) throws Exception {
        SerializedFieldAttributes serializedFieldAttributes = parameterBuilder.serializedFieldAttributes();
        serializedFieldAttributes.setLength(parameterBuilder.length());
        Serializer<?> serializer = getSerializer(parameterBuilder.serializedField().type());
        return serializer.deserialize(getBytes(parameterBuilder.buffer(), parameterBuilder.length()), serializedFieldAttributes);
    }

    private int getLength(SerializedField serializedField, ByteBuffer buffer) {
        int length = serializedField.length();
        if (length == 0 || serializedField.type() == ArraySerializer.class) { // contains variable length objects
            length = IntegerSerializer.getInt(buffer); // for string reads size of the strings, for array reads total bytes in the array
        }
        return length;
    }

    private byte[] getBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

}
