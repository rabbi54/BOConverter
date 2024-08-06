package utils.serializers;

import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StringSerializer implements Serializer<String> {

    @Override
    public byte[] serialize(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] serialize(String value, AnnotationDataClass dataClass) {
        /**
         * If the length specified in the annotation is not provided, the serializer
         * will prefix the serialized string with its length. Otherwise, if the length
         * is specified in the annotation, the string length will not be prefixed.
         */
        ByteBuffer buffer;
        if(dataClass == null)
            throw new NullPointerException("Annotation cannot be null");

        if (dataClass.length == 0) {
            buffer = ByteBuffer.allocate(4 + value.length());
            buffer.putInt(value.length());
        } else {
            buffer = ByteBuffer.allocate(value.length());
        }

        for(int i = 0; i < value.length(); i++) {
            buffer.put((byte) value.charAt(i));
        }
        return buffer.array();
    }

    @Override
    public String deserialize(byte[] data, AnnotationDataClass dataClass) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
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


