package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;

public class BooleanSerializer implements Serializer<Boolean> {

    @Override
    public byte[] serialize(Boolean value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) (value ? 1 : 0));
        return buffer.array();
    }

    @Override
    public Boolean deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte value = buffer.get();
        return (value == 1);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }
}
