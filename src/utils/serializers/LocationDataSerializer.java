package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LocationDataSerializer implements Serializer<Double> {
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
    public Double deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data == null || data.length != 8) {
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
