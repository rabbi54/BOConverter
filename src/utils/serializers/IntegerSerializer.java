package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntegerSerializer implements Serializer<Integer> {

    @Override
    public byte[] serialize(Integer value) {
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        putInt(buffer, value);
        return buffer.array();
    }

    @Override
    public Integer deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return getInt(buffer);
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }

    public static void putInt(ByteBuffer buffer, Integer value) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public static void putInt(ByteBuffer buffer, Integer value, Integer index) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(index, value);
        buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public static Integer getInt(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        Integer value = buffer.getInt();
        buffer.order(ByteOrder.BIG_ENDIAN);
        return value;
    }

    public static Integer getInt(ByteBuffer buffer, Integer index) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        Integer value = buffer.getInt(index);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return value;
    }
}


