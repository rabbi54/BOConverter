package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LongSerializer implements Serializer<Long> {
    @Override
    public byte[] serialize(Long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(value);
        return buffer.array();
    }

    @Override
    public Long deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }
}
