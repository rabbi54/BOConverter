package utils.serializers;

import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ArraySerializer<T> implements Serializer<T> {
    private final Serializer<T> elementSerializer;

    public ArraySerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }


    @Override
    public byte[] serialize(T value) {
        return null;
    }

    @Override
    public byte[] serialize(T array, AnnotationDataClass annotationDataClass) {
        if (!(array instanceof ArrayList)) {
            throw new IllegalArgumentException("Expected an ArrayList");
        }
        ArrayList<T> arrayList = (ArrayList<T>) array;
        ByteBuffer buffer = initializeBuffer();
        serializeArrayElements(arrayList, annotationDataClass, buffer);

        int totalLength = buffer.position() - 4;
        updatePrefix(buffer, annotationDataClass, arrayList.size(), totalLength);

        return finalizeBuffer(buffer);
    }

    private ByteBuffer initializeBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust size as needed
        buffer.position(4); // Reserve the first 4 bytes for the size
        return buffer;
    }

    private void serializeArrayElements(ArrayList<T> arrayList, AnnotationDataClass annotationDataClass, ByteBuffer buffer) {
        for (T element : arrayList) {
            byte[] serializedElement = elementSerializer.serialize(element, annotationDataClass);
            buffer.put(serializedElement);
        }
    }

    private void updatePrefix(ByteBuffer buffer, AnnotationDataClass annotationDataClass, int arraySize, int totalLength) {
        if (annotationDataClass.length != 0) {
            buffer.putInt(0, arraySize * annotationDataClass.length);
        } else {
            buffer.putInt(0, totalLength);
        }
    }

    private byte[] finalizeBuffer(ByteBuffer buffer) {
        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }


    @Override
    public T deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ArrayList<T> arrayList = new ArrayList<>();
        while (buffer.hasRemaining()) {
            int elementLength = getElementLength(dataClass, buffer);
            byte[] bytes = new byte[elementLength];
            buffer.get(bytes);
            T element = elementSerializer.deserialize(bytes, dataClass);
            arrayList.add(element);
        }

        return (T) arrayList;
    }

    private int getElementLength(AnnotationDataClass dataClass, ByteBuffer buffer) {
        int elementLength = dataClass.length;
        if (elementLength == 0) {
            elementLength = buffer.getInt();
        }
        return elementLength;
    }

    @Override
    public Class<T> getType() {
        return elementSerializer.getType();
    }

}
