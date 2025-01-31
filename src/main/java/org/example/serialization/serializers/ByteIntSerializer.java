package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;

public class ByteIntSerializer implements Serializer<Integer> {
    @Override
    public byte[] serialize(Integer value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int firstByte = value & 0xFF;
        buffer.put((byte) firstByte);
        return buffer.array();
    }

    @Override
    public Integer deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length != 1) {
            return null;
        }
        return data[0] & 0xFF;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}
