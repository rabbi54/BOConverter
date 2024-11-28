package serialization.serializers;

import serialization.dataclass.SerializedFieldAttributes;
import serialization.dataclass.SerializationContext;
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
                SerializationContext context = new SerializationContext.Builder()
                        .serializedField(annotation)
                        .buffer(buffer)
                        .field(field)
                        .object(object)
                        .build();
                serializeField(context);
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

    private void serializeField(SerializationContext context) throws Exception {

        Object fieldValue = reflectionManager.getFieldValue(context.field(), context.object());
        if (fieldValue == null && !context.serializedField().required()) {
            return;
        }

        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(context.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);

        SerializationContext serializationContext = new SerializationContext.Builder()
                .from(context)
                .annotationDataClass(serializedFieldAttributes)
                .build();

        if (serializedFieldAttributes.type == Object.class) {
            serializeNestedField(serializationContext);
        } else if (context.serializedField().type() == ArraySerializer.class) {
            serializeArrayField(serializationContext);
        } else {
            serializeSimpleField(serializationContext);
        }
    }

    private void serializeNestedField(SerializationContext context) {
        Object fieldValue = reflectionManager.getFieldValue(context.field(), context.object());
        byte[] nestedSerializedData = serialize(fieldValue);
        if (nestedSerializedData == null) {
            return;
        }
        context.buffer().put(context.serializedField().identifier());
        IntegerSerializer.putInt(context.buffer(), nestedSerializedData.length);
        context.buffer().put(nestedSerializedData);
    }

    private void serializeArrayField(SerializationContext context) throws Exception {
        Serializer<Object> innerSerializer = getSerializerForArrayField(context.serializedField(), context.field(), context.serializedFieldAttributes());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(innerSerializer);

        Object fieldValue = reflectionManager.getFieldValue(context.field(), context.object());
        @SuppressWarnings("unchecked")
        byte[] serializedData = arraySerializer.serialize((ArrayList<Object>) fieldValue, context.serializedFieldAttributes());
        if (serializedData != null) {
            context.buffer().put(context.serializedField().identifier());
            context.buffer().put(serializedData);
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

    private void serializeSimpleField(SerializationContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(context.serializedField().type());

        SerializationCompatibilityValidator.checkSerializerFieldCompatibility(serializer.getClass(), context.field().getType());
        Object fieldValue = reflectionManager.getFieldValue(context.field(), context.object());
        byte[] serializedData = serializer.serialize(fieldValue, context.serializedFieldAttributes());
        if (serializedData == null) {
            return;
        }
        context.buffer().put(context.serializedField().identifier());
        context.buffer().put(serializedData);
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
            SerializationContext serializationContext = new SerializationContext.Builder()
                    .serializedField(serializedField)
                    .buffer(buffer)
                    .clazz(clazz)
                    .object(object)
                    .typeId(typeId)
                    .build();
            addFieldValue(serializationContext);
        }
        return object;
    }

    private void addFieldValue(SerializationContext context) throws Exception {
        int length = getLength(context.serializedField(), context.buffer());
        SerializedFieldAttributes serializedFieldAttributes = serializedFieldManager.getSerializedFieldAttributes(context.serializedField());
        SerializationCompatibilityValidator.checkSupportedSerializer(serializedFieldAttributes.type);
        Field field = serializedFieldManager.findFieldByIdentifier(context.clazz(), serializedFieldAttributes.identifier);
        Object deserializedValue;

        if (field == null) {
            throw new UnsupportedOperationException(context.clazz().getName() + " has no field for typeId: " + context.typeId() + " position " + context.buffer().position());
        }

        SerializationContext serializationContext = new SerializationContext.Builder()
                .from(context)
                .field(field)
                .annotationDataClass(serializedFieldAttributes)
                .length(length)
                .build();
        deserializedValue = getDeserializedValue(serializationContext);
        reflectionManager.setFieldValue(context, field, deserializedValue);
    }

    private Object getDeserializedValue(SerializationContext context) throws Exception {
        Object deserializedValue;
        if (context.serializedFieldAttributes().type == Object.class) {
            byte[] nestedData = getBytes(context.buffer(), context.length());
            deserializedValue = deserialize(nestedData, context.field().getType());
        } else {
            SerializationCompatibilityValidator.checkSerializerFieldCompatibility(context.serializedFieldAttributes().type, context.field().getType());
            deserializedValue = getObject(context);
        }
        return deserializedValue;
    }

    private Object getObject(SerializationContext context) {
        try {
            if (context.serializedField().type() == ArraySerializer.class) {
                return deserializeArrayField(context);
            } else {
                return deserializeField(context);
            }
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    private Object deserializeArrayField(SerializationContext context) throws Exception {
        if (context.serializedField().innerType() == Object.class) {
            Class<?> innerClass = serializedFieldManager.getInnerClass(context.field());
            context.serializedFieldAttributes().setType(innerClass);
            byte[] nestedData = getBytes(context.buffer(), context.length());
            return new ArraySerializer<>(this).deserialize(nestedData, context.serializedFieldAttributes());
        }

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(context.serializedField().innerType());
        ArraySerializer<Object> arraySerializer = new ArraySerializer<>(serializer);
        return arraySerializer.deserialize(getBytes(context.buffer(), context.length()), context.serializedFieldAttributes());
    }

    private Object deserializeField(SerializationContext context) throws Exception {
        SerializedFieldAttributes serializedFieldAttributes = context.serializedFieldAttributes();
        serializedFieldAttributes.setLength(context.length());
        Serializer<?> serializer = getSerializer(context.serializedField().type());
        return serializer.deserialize(getBytes(context.buffer(), context.length()), serializedFieldAttributes);
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
