package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

public class TimeSerializer implements Serializer<Long> {


    @Override
    public byte[] serialize(Long value) {
        StringBuilder timeString = new StringBuilder("00000000");
        byte[] bytes = new byte[4];
        if (value != 0) {
            value = (value - 631152000000L);
            if (value > 0) {
                value /= 1000;
                timeString = new StringBuilder(Long.toHexString(value));
            }
            if (timeString.length() < 8) {
                for (int i = timeString.length(); i < 8; i++)
                    timeString.append('0');
            } else if (timeString.length() > 8) {
                timeString = new StringBuilder("00000000");
            }
        }

        byte[] bArray = hexStringToByteArray(timeString.toString());
        int x = 0;
        for (int i = 3; i >= 0; i--) {
            bytes[x++] = bArray[i];
        }

        return bytes;
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
    public Long deserialize(byte[] data, AnnotationDataClass dataClass) {
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
}