package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LongSerializer implements Serializer<Long> {
    @Override
    public byte[] serialize(Long value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(value);
        return buffer.array();
    }

    @Override
    public Long deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length != 8) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }
}
