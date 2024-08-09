package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

public class LocationDataSerializer implements Serializer<Double> {
    @Override
    public byte[] serialize(Double value) {
        byte[] bytes = new byte[8];
        if (value == 0d) {
            return bytes;
        }
        String val = Long.toHexString(Double.doubleToLongBits(value));
        int first = Integer.parseInt(val.charAt(14) + "" + val.charAt(15), 16);
        int second = Integer.parseInt(val.charAt(12) + "" + val.charAt(13), 16);
        int third = Integer.parseInt(val.charAt(10) + "" + val.charAt(11), 16);
        int fourth = Integer.parseInt(val.charAt(8) + "" + val.charAt(9), 16);
        int fifth = Integer.parseInt(val.charAt(6) + "" + val.charAt(7), 16);
        int sixth = Integer.parseInt(val.charAt(4) + "" + val.charAt(5), 16);
        int seventh = Integer.parseInt(val.charAt(2) + "" + val.charAt(3), 16);
        int eighth = Integer.parseInt(val.charAt(0) + "" + val.charAt(1), 16);

        bytes[0] = (byte) first;
        bytes[1] = (byte) second;
        bytes[2] = (byte) third;
        bytes[3] = (byte) fourth;
        bytes[4] = (byte) fifth;
        bytes[5] = (byte) sixth;
        bytes[6] = (byte) seventh;
        bytes[7] = (byte) eighth;

        return bytes;
    }

    @Override
    public Double deserialize(byte[] data, AnnotationDataClass dataClass) {
        if (data.length != 8) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (byte b : data) {
            result.append(String.format("%02X", b & 0xFF));
        }
        long value = Long.parseLong(result.toString(), 16);
        double d = Double.longBitsToDouble(value);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            d = 0.0d;
        }
        return d;
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }
}
