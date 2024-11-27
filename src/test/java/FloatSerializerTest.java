package test.java;

import org.junit.jupiter.api.Test;
import serialization.serializers.FloatSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class FloatSerializerTest {

    private final FloatSerializer serializer = new FloatSerializer();

    @Test
    public void testSerialize_NonNullValue() {
        Float value = 123.45f;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(value, buffer.getFloat());
    }

    @Test
    public void testSerialize_NullValue() {
        Float value = null;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0.0f, buffer.getFloat());
    }

    @Test
    public void testSerialize_ZeroValue() {
        Float value = 0.0f;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0.0f, buffer.getFloat());
    }

    @Test
    public void testSerialize_PositiveInfinity() {
        Float value = Float.POSITIVE_INFINITY;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(Float.POSITIVE_INFINITY, buffer.getFloat());
    }

    @Test
    public void testSerialize_NegativeInfinity() {
        Float value = Float.NEGATIVE_INFINITY;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(Float.NEGATIVE_INFINITY, buffer.getFloat());
    }

    @Test
    public void testSerialize_NaNValue() {
        Float value = Float.NaN;
        byte[] serialized = serializer.serialize(value);
        assertEquals(4, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertTrue(Float.isNaN(buffer.getFloat()));
    }

    @Test
    public void testDeserialize_ValidData() {
        Float expectedValue = 123.45f;
        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(expectedValue).array();

        Float deserialized = serializer.deserialize(data, null);
        assertEquals(expectedValue, deserialized);
    }

    @Test
    public void testDeserialize_NullData() {
        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(0.0f).array();
        Float deserialized = serializer.deserialize(data, null);
        assertEquals(0.0f, deserialized);
    }

    @Test
    public void testDeserialize_PositiveInfinity() {
        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(Float.POSITIVE_INFINITY).array();
        Float deserialized = serializer.deserialize(data, null);
        assertEquals(Float.POSITIVE_INFINITY, deserialized);
    }

    @Test
    public void testDeserialize_NegativeInfinity() {
        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(Float.NEGATIVE_INFINITY).array();
        Float deserialized = serializer.deserialize(data, null);
        assertEquals(Float.NEGATIVE_INFINITY, deserialized);
    }

    @Test
    public void testDeserialize_NaNValue() {
        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(Float.NaN).array();
        Float deserialized = serializer.deserialize(data, null);
        assertTrue(Float.isNaN(deserialized));
    }
}
