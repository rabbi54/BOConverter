package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

public class FloatSerializer implements Serializer<Float> {

    @Override
    public byte[] serialize(Float value) {
        byte[] bytes = new byte[4];
        if (value == 0d) {
            return bytes;
        }
        String val = Integer.toHexString(Float.floatToIntBits(value));
        int first = Integer.parseInt(val.charAt(6) + "" + val.charAt(7), 16);
        int second = Integer.parseInt(val.charAt(4) + "" + val.charAt(5), 16);
        int third = Integer.parseInt(val.charAt(2) + "" + val.charAt(3), 16);
        int fourth = Integer.parseInt(val.charAt(0) + "" + val.charAt(1), 16);

        bytes[0] = (byte) (first & 0xFF);
        bytes[1] = (byte) (second & 0xFF);
        bytes[2] = (byte) (third & 0xFF);
        bytes[3] = (byte) (fourth & 0xFF);

        return bytes;
    }

    @Override
    public Float deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data.length != Float.BYTES) {
            return null;
        }
        StringBuilder val = new StringBuilder();
        for (int i = Float.BYTES - 1; i >= 0; i--) {
            val.append(String.format("%02X", data[i]));
        }

        long longVal = Long.parseLong(val.toString(), 16);
        float floatVal = Float.intBitsToFloat((int) longVal);
        if (Float.isNaN(floatVal) || Float.isInfinite(floatVal)) {
            return 0.0f;
        }
        return floatVal;
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }
}
