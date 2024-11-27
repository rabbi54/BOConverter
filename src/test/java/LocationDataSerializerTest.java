package test.java;

import org.junit.jupiter.api.Test;
import serialization.serializers.LocationDataSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class LocationDataSerializerTest {

    private final LocationDataSerializer serializer = new LocationDataSerializer();

    @Test
    public void testSerialize_NonNullValue() {
        Double value = 90.1233;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(value, buffer.getDouble());
    }

    @Test
    public void testSerialize_NullValue() {
        Double value = null;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0.0, buffer.getDouble());
    }

    @Test
    public void testSerialize_ZeroValue() {
        Double value = 0.0;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0.0, buffer.getDouble());
    }

    @Test
    public void testSerialize_PositiveInfinity() {
        Double value = Double.POSITIVE_INFINITY;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(Double.POSITIVE_INFINITY, buffer.getDouble());
    }

    @Test
    public void testSerialize_NegativeInfinity() {
        Double value = Double.NEGATIVE_INFINITY;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(Double.NEGATIVE_INFINITY, buffer.getDouble());
    }

    @Test
    public void testSerialize_NaNValue() {
        Double value = Double.NaN;
        byte[] serialized = serializer.serialize(value);
        assertEquals(8, serialized.length);

        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertTrue(Double.isNaN(buffer.getDouble()));
    }

    @Test
    public void testDeserialize_ValidData() {
        Double expectedValue = -90.456;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = buffer.putDouble(expectedValue).array();

        Double deserialized = serializer.deserialize(data, null);
        assertEquals(expectedValue, deserialized);
    }

    @Test
    public void testDeserialize_NullData() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = buffer.putDouble(0.0).array();
        Double deserialized = serializer.deserialize(data, null);
        assertEquals(0.0, deserialized);
    }

    @Test
    public void testDeserialize_PositiveInfinity() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = buffer.putDouble(Double.POSITIVE_INFINITY).array();
        Double deserialized = serializer.deserialize(data, null);
        assertEquals(Double.POSITIVE_INFINITY, deserialized);
    }

    @Test
    public void testDeserialize_NegativeInfinity() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = buffer.putDouble(Double.NEGATIVE_INFINITY).array();
        Double deserialized = serializer.deserialize(data, null);
        assertEquals(Double.NEGATIVE_INFINITY, deserialized);
    }

    @Test
    public void testDeserialize_NaNValue() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = buffer.putDouble(Double.NaN).array();
        Double deserialized = serializer.deserialize(data, null);
        assertTrue(Double.isNaN(deserialized));
    }
}


