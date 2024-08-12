package test.java;

import org.junit.jupiter.api.Test;
import utils.serializers.ShortSerializer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShortSerializerTest {

    private final ShortSerializer serializer = new ShortSerializer();

    @Test
    public void testSerialize() {
        short value = 1245;
        byte[] expectedBytes = ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(value)
                .array();

        byte[] serializedBytes = serializer.serialize(value);
        assertArrayEquals(expectedBytes, serializedBytes, "Serialization did not produce the expected byte array");
    }

    @Test
    public void testDeserialize() {
        short value = 1245;
        byte[] serializedBytes = ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(value)
                .array();

        Short deserializedValue = serializer.deserialize(serializedBytes, null);
        assertEquals(value, deserializedValue, "Deserialization did not produce the expected integer value");
    }

    @Test
    public void testSerializeNull() {
        Short value = 0;
        byte[] serializedBytes = serializer.serialize(null);
        Short deserializedValue = serializer.deserialize(serializedBytes, null);
        assertEquals(value, deserializedValue, "Deserialization did not produce the expected null value");
    }

    @Test
    public void testDeserializeNull() {
        Short deserializedValue = serializer.deserialize(null, null);
        assertNull(deserializedValue, "Deserialization did not produce the expected null value");
    }

    @Test
    public void testDeserializeWithInappropriateByteList() {
        byte[] bytes = new byte[4];
        Short deserializedValue = serializer.deserialize(bytes, null);
        assertNull(deserializedValue, "Deserialization did not produce the expected null value");

        byte[] anotherBytes = new byte[1];
        deserializedValue = serializer.deserialize(anotherBytes, null);
        assertNull(deserializedValue, "Deserialization did not produce the expected null value");
    }
}


