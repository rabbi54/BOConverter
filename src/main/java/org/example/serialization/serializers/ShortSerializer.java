package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShortSerializer implements Serializer<Short> {

    @Override
    public byte[] serialize(Short value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }

    @Override
    public Short deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length != Short.BYTES) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getShort();
    }

    @Override
    public Class<Short> getType() {
        return Short.class;
    }

    @Override
    public Short getDefaultValue() {
        return 0;
    }
}
