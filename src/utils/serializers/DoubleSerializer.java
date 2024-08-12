package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;

public class DoubleSerializer implements Serializer<Double> {
    @Override
    public byte[] serialize(Double value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(value);
        return buffer.array();
    }

    @Override
    public Double deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
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
