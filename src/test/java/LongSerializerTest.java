package test.java;


import org.junit.jupiter.api.Test;
import utils.serializers.LongFrom4ByteSerializer;
import utils.serializers.LongSerializer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class LongSerializerTest {

    private final LongSerializer serializer = new LongSerializer();

    @Test
    public void testSerialize() {
        long value = 123456789L;
        byte[] expectedBytes = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();

        byte[] serializedBytes = serializer.serialize(value);
        assertArrayEquals(expectedBytes, serializedBytes, "Serialization did not produce the expected byte array");
    }

    @Test
    public void testDeserialize() {
        long value = 123456789L;
        byte[] serializedBytes = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();

        Long deserializedValue = serializer.deserialize(serializedBytes, null);
        assertEquals(value, deserializedValue, "Deserialization did not produce the expected integer value");
    }


    @Test
    public void testSerializeLength() {
        Long value = 123456789L;
        byte[] serializedBytes = serializer.serialize(value);
        assertEquals(serializedBytes.length, 8, "Serialization did not produce the expected length");
    }

    @Test
    public void testSerializeWithNull() {
        byte[] serializedBytes = serializer.serialize(null);
        Long value = serializer.deserialize(serializedBytes, null);
        assertEquals(0L, value, "Null deserialization did not produce the expected value");
    }

    @Test
    public void testSerializeWithNegativeValue() {
        Long expected = -Long.MAX_VALUE;
        byte[] serializedBytes = serializer.serialize(expected);
        Long returned = serializer.deserialize(serializedBytes, null);
        assertEquals(returned, expected, "Negative deserialization did not produce the expected value");
    }

    @Test
    public void testSerializeWithInvalidByteSize() {
        byte[] serializedBytes = new byte[4];
        Long returned = serializer.deserialize(serializedBytes, null);
        assertNull(returned, "Invalid deserialization did not produce the expected value");
    }
}



