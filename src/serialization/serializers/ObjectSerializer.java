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

    private void serializeField(SerializationParameter parameter) throws Exception {

        Object fieldValue = reflectionManager.getFieldValue(parameter.field(), parameter.object());
        if (fieldValue == null && !parameter.serializedField().required()) {
            return;
        }

        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(parameter.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);

        SerializationParameter serializationParameter = new SerializationParameter.Builder()
                .from(parameter)
                .annotationDataClass(serializedFieldAttributes)
                .build();

        if (serializedFieldAttributes.type == Object.class) {
            serializeNestedField(serializationParameter);
        } else if (parameter.serializedField().type() == ArraySerializer.class) {
            serializeArrayField(serializationParameter);
        } else {
            serializeSimpleField(serializationParameter);
        }
    }

    private void serializeNestedField(SerializationParameter parameter) {
        Object fieldValue = reflectionManager.getFieldValue(parameter.field(), parameter.object());
        byte[] nestedSerializedData = serialize(fieldValue);
        if (nestedSerializedData == null) {
            return;
        }
        parameter.buffer().put(parameter.serializedField().identifier());
        IntegerSerializer.putInt(parameter.buffer(), nestedSerializedData.length);
        parameter.buffer().put(nestedSerializedData);
    }

    private void serializeArrayField(SerializationParameter parameter) throws Exception {
        Serializer<Object> innerSerializer = getSerializerForArrayField(parameter.serializedField(), parameter.field(), parameter.serializedFieldAttributes());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);

        Object fieldValue = reflectionManager.getFieldValue(parameter.field(), parameter.object());
        @SuppressWarnings("unchecked")
        byte[] serializedData = arraySerializer.serialize((ArrayList<Object>) fieldValue, parameter.serializedFieldAttributes());
        if (serializedData != null) {
            parameter.buffer().put(parameter.serializedField().identifier());
            parameter.buffer().put(serializedData);
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

    private void serializeSimpleField(SerializationParameter parameter) throws Exception {
        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(parameter.serializedField().type());

        SerializationCompatibilityValidator.checkSerializerFieldCompatibility(serializer.getClass(), parameter.field().getType());
        Object fieldValue = reflectionManager.getFieldValue(parameter.field(), parameter.object());
        byte[] serializedData = serializer.serialize(fieldValue, parameter.serializedFieldAttributes());
        if (serializedData == null) {
            return;
        }
        parameter.buffer().put(parameter.serializedField().identifier());
        parameter.buffer().put(serializedData);
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

    private void addFieldValue(SerializationParameter parameter) throws Exception {
        int length = getLength(parameter.serializedField(), parameter.buffer());
        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(parameter.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);
        Field field = serializedFieldManager.findFieldByIdentifier(parameter.clazz(), serializedFieldAttributes.identifier);
        Object deserializedValue;

        if (field == null) {
            throw new UnsupportedOperationException(parameter.clazz().getName() + " has no field for typeId: " + parameter.typeId() + " position " + parameter.buffer().position());
        }

        SerializationParameter builder = new SerializationParameter.Builder()
                .from(parameter)
                .field(field)
                .annotationDataClass(serializedFieldAttributes)
                .length(length)
                .build();
        deserializedValue = getDeserializedValue(builder);
        reflectionManager.setFieldValue(parameter, field, deserializedValue);
    }

    private Object getDeserializedValue(SerializationParameter parameter) throws Exception {
        Object deserializedValue;
        if (parameter.serializedFieldAttributes().type == Object.class) {
            byte[] nestedData = getBytes(parameter.buffer(), parameter.length());
            deserializedValue = deserialize(nestedData, parameter.field().getType());
        } else {
            SerializationCompatibilityValidator.checkSerializerFieldCompatibility(parameter.serializedFieldAttributes().type, parameter.field().getType());
            deserializedValue = getObject(parameter);
        }
        return deserializedValue;
    }

    private Object getObject(SerializationParameter parameter) {
        try {
            if (parameter.serializedField().type() == ArraySerializer.class) {
                return deserializeArrayField(parameter);
            } else {
                return deserializeField(parameter);
            }
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    private Object deserializeArrayField(SerializationParameter parameter) throws Exception {
        if (parameter.serializedField().innerType() == Object.class) {
            Class<?> innerClass = serializedFieldManager.getInnerClass(parameter.field());
            parameter.serializedFieldAttributes().setType(innerClass);
            byte[] nestedData = getBytes(parameter.buffer(), parameter.length());
            return new ArraySerializer<>(this).deserialize(nestedData, parameter.serializedFieldAttributes());
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(parameter.serializedField().innerType());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
        return arraySerializer.deserialize(getBytes(parameter.buffer(), parameter.length()), parameter.serializedFieldAttributes());
    }

    private Object deserializeField(SerializationParameter parameter) throws Exception {
        SerializedFieldAttributes serializedFieldAttributes = parameter.serializedFieldAttributes();
        serializedFieldAttributes.setLength(parameter.length());
        Serializer<?> serializer = getSerializer(parameter.serializedField().type());
        return serializer.deserialize(getBytes(parameter.buffer(), parameter.length()), serializedFieldAttributes);
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
