package org.example.serialization.serializers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DoubleSerializer implements Serializer<Double> {
    @Override
    public byte[] serialize(Double value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(value);
        return buffer.array();
    }

    @Override
    public Double deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length != Double.BYTES) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getDouble();
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

    @Override
    public Double getDefaultValue() {
        return 0.0;
    }
}
