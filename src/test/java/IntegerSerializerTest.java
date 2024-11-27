package test.java;

import org.junit.jupiter.api.Test;
import serialization.serializers.IntegerSerializer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntegerSerializerTest {

    private final IntegerSerializer serializer = new IntegerSerializer();

    @Test
    public void testSerialize() {
        int value = 123456789;
        byte[] expectedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();

        byte[] serializedBytes = serializer.serialize(value);
        assertArrayEquals(expectedBytes, serializedBytes, "Serialization did not produce the expected byte array");
    }

    @Test
    public void testDeserialize() {
        Integer value = 123456789;
        byte[] serializedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();

        Integer deserializedValue = serializer.deserialize(serializedBytes, null);
        assertEquals(value, deserializedValue, "Deserialization did not produce the expected integer value");
    }

    @Test
    public void testPutInt() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        int value = 123456789;

        IntegerSerializer.putInt(buffer, value);
        buffer.flip();
        byte[] expectedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();

        assertArrayEquals(expectedBytes, buffer.array(), "putInt did not produce the expected byte array");
    }

    @Test
    public void testGetInt() {
        Integer value = 123456789;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        IntegerSerializer.putInt(buffer, value);
        buffer.flip();

        Integer retrievedValue = IntegerSerializer.getInt(buffer);
        assertEquals(value, retrievedValue, "getInt did not retrieve the expected integer value");
    }

    @Test
    public void testPutIntWithIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        int value = 123456789;
        int index = 4;

        IntegerSerializer.putInt(buffer, value, index);
        buffer.position(index);
        byte[] expectedBytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();

        byte[] resultBytes = new byte[4];
        buffer.get(resultBytes);
        assertArrayEquals(expectedBytes, resultBytes, "putInt with index did not produce the expected byte array");
    }

    @Test
    public void testGetIntWithIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        Integer value = 123456789;
        Integer index = 4;
        IntegerSerializer.putInt(buffer, value, index);

        Integer retrievedValue = IntegerSerializer.getInt(buffer, index);
        assertEquals(value, retrievedValue, "getInt with index did not retrieve the expected integer value");
    }
}

