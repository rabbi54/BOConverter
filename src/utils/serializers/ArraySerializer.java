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

        ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust size as needed

        buffer.putInt(arrayList.size() * annotationDataClass.length);
        for (T element : arrayList) {
            byte[] serializedElement = elementSerializer.serialize(element);
            buffer.put(serializedElement);
        }

        byte[] result = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(result);
        return result;
    }

    @Override
    public T deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int elementsCount = dataClass.length / dataClass.length;
        @SuppressWarnings("unchecked")
        ArrayList<T> arrayList = new ArrayList<>(elementsCount);

        while (buffer.hasRemaining()) {
            int elementLength = dataClass.length;
            byte[] bytes = new byte[elementLength];
            buffer.get(bytes);
            T element = elementSerializer.deserialize(bytes, dataClass);
            arrayList.add(element);
        }

        return (T) arrayList;
    }

    @Override
    public Class<T> getType() {
        return elementSerializer.getType();
    }

}
