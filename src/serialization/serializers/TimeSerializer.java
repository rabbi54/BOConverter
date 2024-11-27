package serialization.serializers;

import serialization.dataclass.SerializedFieldAttributes;
import serialization.interfaces.Serializer;

public class TimeSerializer implements Serializer<Long> {


    @Override
    public byte[] serialize(Long value) {
        if (value == null) {
            value = getDefaultValue();
        }
        StringBuilder timeString = new StringBuilder("00000000");
        byte[] bytes = new byte[4];
        timeString = getProcessedTimeString(value, timeString);

        byte[] bArray = hexStringToByteArray(timeString.toString());
        int x = 0;
        for (int i = 3; i >= 0; i--) {
            bytes[x++] = bArray[i];
        }

        return bytes;
    }

    private StringBuilder getProcessedTimeString(Long value, StringBuilder timeString) {
        if (value != 0) {
            value = (value - 631152000000L);
            if (value > 0) {
                value /= 1000;
                timeString = new StringBuilder(Long.toHexString(value));
            }
            if (timeString.length() < 8) {
                timeString.append("0".repeat(Math.max(0, 8 - timeString.length())));
            } else if (timeString.length() > 8) {
                timeString = new StringBuilder("00000000");
            }
        }
        return timeString;
    }

    private byte[] hexStringToByteArray(String s) {
        if (s == null || s.isEmpty() || s.length() % 2 != 0) {
            return null;
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
            + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public Long deserialize(byte[] data, SerializedFieldAttributes fieldAttributes) {
        if (data == null || data.length < 4) {
            return null;
        }
        StringBuilder timeString = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            timeString.append(String.format("%02X", data[i]));
        }
        long time = Long.parseLong(timeString.toString(), 16);
        if (time != 0) {
            time = (time * 1000) + 631152000000L;
        }
        return time;
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }
}
