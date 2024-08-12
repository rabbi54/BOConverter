package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;

public class ByteIntSerializer implements Serializer<Integer> {
    @Override
    public byte[] serialize(Integer value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int firstByte = value & 0xFF;
        buffer.put((byte) firstByte);
        return buffer.array();
    }

    @Override
    public Integer deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data == null || data.length != 1) {
            return null;
        }
        return data[0] & 0xFF;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}
