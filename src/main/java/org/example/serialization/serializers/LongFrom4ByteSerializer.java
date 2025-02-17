package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LongFrom4ByteSerializer implements Serializer<Long> {

    @Override
    public byte[] serialize(Long value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value.intValue());
        return buffer.array();
    }

    @Override
    public Long deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length != 4) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return (long) buffer.getInt();
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
