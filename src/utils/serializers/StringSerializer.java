package utils.serializers;

import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
        if (value == null) {
            value = getDefaultValue();
        }
        ByteBuffer buffer;
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if(dataClass == null)
            throw new NullPointerException("Annotation cannot be null");

        if (dataClass.length == 0) {
            buffer = ByteBuffer.allocate(4 + bytes.length);
            IntegerSerializer.putInt(buffer, bytes.length);
        } else {
            buffer = ByteBuffer.allocate(bytes.length);
        }

        for (byte aByte : bytes) {
            buffer.put(aByte);
        }
        return buffer.array();
    }

    @Override
    public String deserialize(byte[] data, AnnotationDataClass dataClass) {
        if(data.length != dataClass.length) {
            return null;
        }

        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getDefaultValue() {
        return "";
    }

}


