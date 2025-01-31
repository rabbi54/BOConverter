package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.example.serialization.SerializationConstants.MAX_BUFFER_SIZE;

public class ArraySerializer<T> implements Serializer<ArrayList<T>> {
    private final Serializer<T> elementSerializer;

    public ArraySerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }


    @Override
    public byte[] serialize(ArrayList<T> array) {
        return null;
    }

    @Override
    public byte[] serialize(ArrayList<T> array, SerializedFieldAttributes serializedFieldAttributes) {
        if (array == null) {
            array = getDefaultValue();
        }
        ArrayList<T> arrayList = array;
        ByteBuffer buffer = initializeBuffer();
        serializeArrayElements(arrayList, serializedFieldAttributes, buffer);

        int totalLength = buffer.position() - 4;
        updatePrefix(buffer, serializedFieldAttributes, arrayList.size(), totalLength);

        return finalizeBuffer(buffer);
    }

    private ByteBuffer initializeBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE); // Adjust size as needed
        buffer.position(4); // Reserve the first 4 bytes for the size
        return buffer;
    }

    private void serializeArrayElements(ArrayList<T> arrayList, SerializedFieldAttributes serializedFieldAttributes, ByteBuffer buffer) {
        for (T element : arrayList) {
            byte[] serializedElement = serializeElement(element, serializedFieldAttributes);
            buffer.put(serializedElement);
        }
    }

    private byte[] serializeElement(T element, SerializedFieldAttributes serializedFieldAttributes) {
        try {
            byte[] serializedData = elementSerializer.serialize(element, serializedFieldAttributes);

            if (serializedFieldAttributes.type == Object.class) {
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + serializedData.length);
                IntegerSerializer.putInt(buffer, serializedData.length);
                buffer.put(serializedData);
                return buffer.array();
            }
            return serializedData;

        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    private void updatePrefix(ByteBuffer buffer, SerializedFieldAttributes serializedFieldAttributes, int arraySize, int totalLength) {
        if (serializedFieldAttributes.length != 0) {
            IntegerSerializer.putInt(buffer, arraySize * serializedFieldAttributes.length, 0);
        } else {
            IntegerSerializer.putInt(buffer, totalLength, 0);
        }
    }

    private byte[] finalizeBuffer(ByteBuffer buffer) {
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }


    @Override
    public ArrayList<T> deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ArrayList<T> arrayList = new ArrayList<>();
        SerializedFieldAttributes innerTypeAnnotation = new SerializedFieldAttributes(
                fieldAttributes.getType(),
                fieldAttributes.getIdentifier(),
                fieldAttributes.getLength(),
                fieldAttributes.getIsRequired()
        );
        int annotationLength = innerTypeAnnotation.length;
        while (buffer.hasRemaining()) {
            int elementLength = annotationLength > 0 ? innerTypeAnnotation.length : getElementLength(fieldAttributes, buffer);
            byte[] bytes = new byte[elementLength];
            buffer.get(bytes);
            innerTypeAnnotation.setLength(elementLength);
            T element = elementSerializer.deserialize(bytes, innerTypeAnnotation);
            arrayList.add(element);
        }

        return arrayList;
    }

    private int getElementLength(SerializedFieldAttributes fieldAttributes, ByteBuffer buffer) {
        int elementLength = fieldAttributes.length;
        if (elementLength == 0) {
            elementLength = IntegerSerializer.getInt(buffer);
        }
        return elementLength;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<ArrayList<T>> getType() {
        return (Class<ArrayList<T>>) elementSerializer.getType();
    }

    @Override
    public ArrayList<T> getDefaultValue() {
        return new ArrayList<>();
    }

}
