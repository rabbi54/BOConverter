package test.java;

import org.junit.jupiter.api.Test;
import serialization.serializers.ByteIntSerializer;

import static org.junit.jupiter.api.Assertions.*;

public class ByteIntSerializerTest {

    private final ByteIntSerializer serializer = new ByteIntSerializer();

    @Test
    public void testSerialize_NonNullValue() {
        Integer value = 127;
        byte[] serialized = serializer.serialize(value);
        assertEquals(1, serialized.length);
        assertEquals((byte) 127, serialized[0]);
    }

    @Test
    public void testSerialize_NullValue() {
        Integer value = null;
        byte[] serialized = serializer.serialize(value);
        assertEquals(1, serialized.length);
        assertEquals((byte) 0, serialized[0]); // Assuming the default value is 0
    }

    @Test
    public void testSerialize_ZeroValue() {
        Integer value = 0;
        byte[] serialized = serializer.serialize(value);
        assertEquals(1, serialized.length);
        assertEquals((byte) 0, serialized[0]);
    }

    @Test
    public void testSerialize_MaxValue() {
        Integer value = 255;
        byte[] serialized = serializer.serialize(value);
        assertEquals(1, serialized.length);
        assertEquals((byte) 255, serialized[0]);
    }

    @Test
    public void testDeserialize_ValidData() {
        byte[] data = {(byte) 127};
        Integer deserialized = serializer.deserialize(data, null);
        assertEquals(127, deserialized);
    }

    @Test
    public void testDeserialize_ZeroData() {
        byte[] data = {(byte) 0};
        Integer deserialized = serializer.deserialize(data, null);
        assertEquals(0, deserialized);
    }

    @Test
    public void testDeserialize_MaxValueData() {
        byte[] data = {(byte) 255};
        Integer deserialized = serializer.deserialize(data, null);
        assertEquals(255, deserialized);
    }

    @Test
    public void testDeserialize_MinValueData() {
        byte[] data = {(byte) -1};
        Integer deserialized = serializer.deserialize(data, null);
        assertEquals(255, deserialized); // -1 should be interpreted as 255 (unsigned)
    }
}
