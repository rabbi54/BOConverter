package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDSerializer implements Serializer<String> {

    @Override
    public byte[] serialize(String uuid) {
        if (uuid == null) {
            uuid = getDefaultValue();
        }
        byte[] bytes = new byte[16];
        if (uuid == null || uuid.isEmpty()) {
            return bytes;
        }
        uuid = uuid.replaceAll("-", "");
        if (uuid.length() == 32) {
            for (int i = 0; i < 16; i++) {
                bytes[15 - i] = (byte) (Integer.parseInt(uuid.substring(i*2, i*2+2), 16) & 0xFF);
            }
        }
        return bytes;
    }

    @Override
    public String deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data == null || data.length < 15) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 15; i >= 0; i--) {
            builder.append(String.format("%02x", data[i] & 0xFF));
        }
        builder.insert(8, "-");
        builder.insert(13, "-");
        builder.insert(18, "-");
        builder.insert(23, "-");

        return builder.toString();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getDefaultValue() {
        return "00000000-0000-0000-0000-000000000000";
    }
}
