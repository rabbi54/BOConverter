package test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.dataclass.AnnotationDataClass;
import utils.serializers.TimeSerializer;

import static org.junit.jupiter.api.Assertions.*;

class TimeSerializerTest {

    private TimeSerializer timeSerializer;
    private AnnotationDataClass annotationDataClass;

    @BeforeEach
    void setUp() {
        timeSerializer = new TimeSerializer();
        annotationDataClass = null;
    }

    @Test
    void testSerialize_NullValue() {
        byte[] result = timeSerializer.serialize(null);
        assertNotNull(result);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result, "Null value should serialize to 4 zero bytes.");
    }

    @Test
    void testSerialize_ZeroValue() {
        byte[] result = timeSerializer.serialize(0L);
        assertNotNull(result);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result, "Zero value should serialize to 4 zero bytes.");
    }

    @Test
    void testSerialize_MinimumTime() {
        // This tests the smallest valid time after the epoch offset
        long minValue = 631152000000L;
        byte[] result = timeSerializer.serialize(minValue);
        assertNotNull(result);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result, "Minimum time should serialize to 4 zero bytes.");
    }

    @Test
    void testSerialize_MaximumTime() {
        // This tests the maximum time that can be serialized within the 4-byte limit
        long maxValue = 4294967295L * 1000 + 631152000000L;
        byte[] result = timeSerializer.serialize(maxValue);
        assertNotNull(result);
        assertArrayEquals(new byte[]{-1, -1, -1, -1}, result, "Maximum time should serialize to 4 FF bytes.");
    }

    @Test
    void testDeserialize_NullData() {
        Long result = timeSerializer.deserialize(null, annotationDataClass);
        assertNull(result, "Deserializing null data should return null.");
    }

    @Test
    void testDeserialize_ZeroBytes() {
        byte[] data = {0, 0, 0, 0};
        Long result = timeSerializer.deserialize(data, annotationDataClass);
        assertEquals(0L, result, "Deserializing zero bytes should return 0L.");
    }

    @Test
    void testDeserialize_MinimumTimeBytes() {
        byte[] data = {0, 0, 0, 0};
        Long result = timeSerializer.deserialize(data, annotationDataClass);
        assertEquals(0, result, "Deserializing zero bytes should return the minimum time.");
    }

    @Test
    void testDeserialize_MaximumTimeBytes() {
        byte[] data = {-1, -1, -1, -1};
        Long result = timeSerializer.deserialize(data, annotationDataClass);
        assertEquals(4294967295L * 1000 + 631152000000L, result, "Deserializing 4 FF bytes should return the maximum time.");
    }

    @Test
    void testSerializeAndDeserialize() {
        long originalValue = System.currentTimeMillis();
        byte[] serializedData = timeSerializer.serialize(originalValue);
        Long deserializedValue = timeSerializer.deserialize(serializedData, annotationDataClass);
        assertEquals(originalValue / 1000 * 1000, deserializedValue, "Deserialized value should match the original (rounded to the nearest second).");
    }
}

