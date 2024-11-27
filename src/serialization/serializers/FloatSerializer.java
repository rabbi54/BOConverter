package serialization.serializers;

import serialization.dataclass.AnnotationDataClass;
import serialization.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FloatSerializer implements Serializer<Float> {

    @Override
    public byte[] serialize(Float value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        return buffer.array();
    }

    @Override
    public Float deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data == null || data.length != Float.BYTES) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public Float getDefaultValue() {
        return 0f;
    }
}
