package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;

public class BooleanSerializer implements Serializer<Boolean> {

    @Override
    public byte[] serialize(Boolean value) {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) (value ? 1 : 0));
        return buffer.array();
    }

    @Override
    public Boolean deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte value = buffer.get();
        return (value == 1);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}
