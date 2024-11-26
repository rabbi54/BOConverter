package test.java;

import org.junit.jupiter.api.Test;
import utils.serializers.LongFrom4ByteSerializer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LongFrom4SerializedFieldTest {

    private final LongFrom4ByteSerializer serializer = new LongFrom4ByteSerializer();

    @Test
    public void testSerialize() {
        Long value = 123456789L;
        byte[] expectedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value.intValue())
                .array();

        byte[] serializedBytes = serializer.serialize(value);
        assertArrayEquals(expectedBytes, serializedBytes, "Serialization did not produce the expected byte array");
    }

    @Test
    public void testDeserialize() {
        Long value = 123456789L;
        byte[] serializedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value.intValue())
                .array();

        Long deserializedValue = serializer.deserialize(serializedBytes, null);
        assertEquals(value, deserializedValue, "Deserialization did not produce the expected integer value");
    }


    @Test
    public void testSerializeLength() {
        Long value = 123456789L;
        byte[] serializedBytes = serializer.serialize(value);
        assertEquals(serializedBytes.length, 4, "Serialization did not produce the expected length");
    }

    @Test
    public void testSerializeWithNull() {
        byte[] serializedBytes = serializer.serialize(null);
        Long value = serializer.deserialize(serializedBytes, null);
        assertEquals(0L, value, "Null deserialization did not produce the expected value");
    }

    @Test
    public void testSerializeWithNegativeValue() {
        Long expected = (long) -Integer.MAX_VALUE;
        byte[] serializedBytes = serializer.serialize(expected);
        Long returned = serializer.deserialize(serializedBytes, null);
        assertEquals(returned, expected, "Negative deserialization did not produce the expected value");
    }
}


