package test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.serializers.UUIDSerializer;


import static org.junit.jupiter.api.Assertions.*;

class UUIDSerializerTest {

    private UUIDSerializer uuidSerializer;

    @BeforeEach
    void setUp() {
        uuidSerializer = new UUIDSerializer();
    }

    @Test
    void testSerialize_NullUUID() {
        byte[] result = uuidSerializer.serialize(null);
        assertNotNull(result);
        assertEquals(16, result.length);
        for (byte b : result) {
            assertEquals(0, b);
        }
    }

    @Test
    void testSerialize_EmptyUUID() {
        byte[] result = uuidSerializer.serialize("");
        assertNotNull(result);
        assertEquals(16, result.length);
        for (byte b : result) {
            assertEquals(0, b);
        }
    }

    @Test
    void testSerialize_ValidUUID() {
        String uuid = "12ca2200-0000-0000-0000-00000000f9ab";
        byte[] result = uuidSerializer.serialize(uuid);
        assertNotNull(result);
        assertEquals(16, result.length);

        // Check individual bytes
        byte[] expectedBytes = new byte[]{
                (byte) 0xab, (byte) 0xf9, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x22, (byte) 0xca, (byte) 0x12
        };
        assertArrayEquals(expectedBytes, result);
    }

    @Test
    void testDeserialize_NullData() {
        String result = uuidSerializer.deserialize(null, null);
        assertNull(result);
    }

    @Test
    void testDeserialize_InvalidData() {
        byte[] data = new byte[]{0x00};
        String result = uuidSerializer.deserialize(data, null);
        assertNull(result);
    }

    @Test
    void testDeserialize_ValidData() {
        byte[] data = new byte[]{
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0,
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
        };
        String result = uuidSerializer.deserialize(data, null);
        assertEquals("f0debc9a-7856-3412-f0de-bc9a78563412", result);
    }

    @Test
    void testGetType() {
        assertEquals(String.class, uuidSerializer.getType());
    }

    @Test
    void testGetDefaultValue() {
        assertEquals("00000000-0000-0000-0000-000000000000", uuidSerializer.getDefaultValue());
    }
}
