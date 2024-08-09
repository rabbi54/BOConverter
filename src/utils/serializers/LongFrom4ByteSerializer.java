package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LongFrom4ByteSerializer implements Serializer<Long> {

    @Override
    public byte[] serialize(Long value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value.intValue());
        return buffer.array();
    }

    @Override
    public Long deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data.length != 4) {
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
}
