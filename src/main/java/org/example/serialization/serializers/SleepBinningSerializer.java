package org.example.serialization.serializers;

import org.example.models.SleepBinning;
import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;

import java.nio.ByteBuffer;

public class SleepBinningSerializer implements Serializer<SleepBinning> {
    @Override
    public byte[] serialize(SleepBinning value) {
        if (value == null) {
            value = getDefaultValue();
        }

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(value.getHrri());
        buffer.putInt(value.getHrss());
        return buffer.array();
    }

    @Override
    public SleepBinning deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int hrri = buffer.getInt();
        int hrss = buffer.getInt();
        return new SleepBinning(hrri, hrss);
    }

    @Override
    public Class<SleepBinning> getType() {
        return SleepBinning.class;
    }

    @Override
    public SleepBinning getDefaultValue() {
        return new SleepBinning(0, 0);
    }
}
