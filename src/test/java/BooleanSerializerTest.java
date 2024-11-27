package test.java;


import org.junit.jupiter.api.Test;
import serialization.serializers.BooleanSerializer;

import static org.junit.jupiter.api.Assertions.*;

class BooleanSerializerTest {

    private final BooleanSerializer serializer = new BooleanSerializer();

    @Test
    public void testSerializeTrue() {
        Boolean value = true;
        byte[] expected = new byte[]{1};
        byte[] result = serializer.serialize(value);
        assertArrayEquals(expected, result, "Serialization of 'true' failed.");
    }

    @Test
    public void testSerializeFalse() {
        Boolean value = false;
        byte[] expected = new byte[]{0};
        byte[] result = serializer.serialize(value);
        assertArrayEquals(expected, result, "Serialization of 'false' failed.");
    }

    @Test
    public void testSerializeNull() {
        Boolean value = null;
        byte[] expected = new byte[]{0}; // Assuming the default value is false
        byte[] result = serializer.serialize(value);
        assertArrayEquals(expected, result, "Serialization of 'null' failed.");
    }

    @Test
    public void testDeserializeTrue() {
        byte[] data = new byte[]{1};
        Boolean expected = true;
        Boolean result = serializer.deserialize(data, null);
        assertEquals(expected, result, "Deserialization of '1' to true failed.");
    }

    @Test
    public void testDeserializeFalse() {
        byte[] data = new byte[]{0};
        Boolean expected = false;
        Boolean result = serializer.deserialize(data, null);
        assertEquals(expected, result, "Deserialization of '0' to false failed.");
    }

    @Test
    public void testDeserializeInvalidData() {
        byte[] data = new byte[]{2}; // Invalid byte for a boolean
        Boolean expected = false; // Assuming it defaults to false
        Boolean result = serializer.deserialize(data, null);
        assertEquals(expected, result, "Deserialization of invalid byte should default to false.");
    }
}

