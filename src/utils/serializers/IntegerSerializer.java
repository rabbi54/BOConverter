package utils.serializers;

import utils.interfaces.Serializer;

import java.nio.ByteBuffer;

public class IntegerSerializer implements Serializer<Integer> {

    @Override
    public byte[] serialize(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 byte type + 4 bytes integer
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    public Integer deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return buffer.getInt();
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}


