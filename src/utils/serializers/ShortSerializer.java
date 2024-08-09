package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShortSerializer implements Serializer<Short> {

    @Override
    public byte[] serialize(Short value) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }

    @Override
    public Short deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data.length != Short.BYTES) {
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
}
