package utils.serializers;

import utils.interfaces.Serializer;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class StringSerializer implements Serializer<String> {

    @Override
    public byte[] serialize(String value) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + value.length());
        buffer.putInt(value.length());
        for(int i = 0; i < value.length(); i++) {
            buffer.put((byte) value.charAt(i));
        }
        return buffer.array();
    }

    @Override
    public String deserialize(ByteSerializerDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(dataClass.data);
        int stringLen = dataClass.length;
        byte[] stringBytes = new byte[stringLen];
        buffer.get(stringBytes, 0, stringLen);
        return new String(stringBytes);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}


