package utils.serializers;

import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDSerializer implements Serializer<UUID> {

    @Override
    public byte[] serialize(UUID uuid) {
        // Convert UUID to a 16-byte array
        ByteBuffer buffer = ByteBuffer.allocate(16); // 1 byte type + 16 bytes UUID
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    @Override
    public UUID deserialize(ByteSerializerDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(dataClass.data);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }

    // Additional utility method to convert UUID to hex string
    public static String uuidToHexString(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = buffer.array();
        return bytesToHex(bytes);
    }

    // Additional utility method to convert hex string to UUID
    public static UUID hexStringToUUID(String hexString) {
        byte[] bytes = hexStringToBytes(hexString);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
