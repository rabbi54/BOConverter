package test.java;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import serialization.dataclass.AnnotationDataClass;
import serialization.serializers.StringSerializer;

import java.util.Arrays;

class StringSerializerTest {

    private final StringSerializer serializer = new StringSerializer();

    @Test
    void testSerializeWithLengthPrefix() {
        String value = "Hello, World!";
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, 0, true);
        byte[] result = serializer.serialize(value, annotation);

        assertNotNull(result);
        assertEquals(4 + value.length(), result.length);

        // The first 4 bytes should contain the length of the string
        int length = (result[3] << 24) + ((result[2] & 0xFF) << 16) + ((result[1] & 0xFF) << 8) + (result[0] & 0xFF);
        assertEquals(value.length(), length);

        // The remaining bytes should be the UTF-8 encoded string
        String deserializedValue = new String(result, 4, value.length());
        assertEquals(value, deserializedValue);
    }

    @Test
    void testSerializeWithoutLengthPrefix() {
        String value = "Hello, World!";
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, value.length(), true);
        byte[] result = serializer.serialize(value, annotation);

        assertNotNull(result);
        assertEquals(value.length(), result.length);

        String deserializedValue = new String(result);
        assertEquals(value, deserializedValue);
    }

    @Test
    void testSerializeNullValue() {
        String value = null;
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, 0, true);
        byte[] result = serializer.serialize(value, annotation);

        assertNotNull(result);
        assertEquals(4, result.length);
        // The first 4 bytes should contain the length of the string, which is 0
        int length = (result[3] << 24) + ((result[2] & 0xFF) << 16) + ((result[1] & 0xFF) << 8) + (result[0] & 0xFF);

        assertEquals(0, length);
    }

    @Test
    void testDeserializeWithLengthPrefix() {
        String value = "Hello, World!";
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, 0, true);
        byte[] result = serializer.serialize(value, annotation);
        int length = (result[3] << 24) + ((result[2] & 0xFF) << 16) + ((result[1] & 0xFF) << 8) + (result[0] & 0xFF);
        annotation.setLength(length);
        byte[] withoutPrefix = Arrays.copyOfRange(result, 4, result.length);
        String actual = serializer.deserialize(withoutPrefix, annotation);
        assertNotNull(actual);
        assertEquals(value, actual);
    }

    @Test
    void testDeserializeWithoutLengthPrefix() {
        String value = "Hello, World!";
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, value.length(), true);
        byte[] data = serializer.serialize(value, annotation);
        String result = serializer.deserialize(data, annotation);

        assertNotNull(result);
        assertEquals(value, result);
    }

    @Test
    void testDeserializeNullData() {
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, 0, true);
        String result = serializer.deserialize(null, annotation);

        assertNull(result);
    }

    @Test
    void testDeserializeIncorrectLength() {
        String value = "Hello";
        AnnotationDataClass annotation = new AnnotationDataClass(String.class, (byte)1, 10, true);
        byte[] data = value.getBytes();
        String result = serializer.deserialize(data, annotation);

        assertNull(result);
    }

    @Test
    void testDeserializeNullAnnotationDataClass() {
        assertThrows(NullPointerException.class, () -> serializer.serialize("Test", null));
    }
}

