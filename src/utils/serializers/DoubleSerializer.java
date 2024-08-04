package utils.serializers;

import utils.interfaces.Serializer;

import java.nio.ByteBuffer;

public class DoubleSerializer implements Serializer<Double> {
    @Override
    public byte[] serialize(Double value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(value);
        return buffer.array();
    }

    @Override
    public Double deserialize(ByteSerializerDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(dataClass.data);
        return buffer.getDouble();
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }
}
