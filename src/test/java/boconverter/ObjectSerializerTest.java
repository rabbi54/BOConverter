package boconverter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.exceptions.SerializerCreationException;
import org.example.serialization.interfaces.SerializedField;
import org.example.serialization.interfaces.Serializer;
import org.example.serialization.serializers.DoubleSerializer;
import org.example.serialization.serializers.IntegerSerializer;
import org.example.serialization.serializers.ObjectSerializer;
import org.example.serialization.serializers.StringSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSerializerTest {

    static class InaccessibleConstructorSerializer implements Serializer<String> {
        private InaccessibleConstructorSerializer() {} // Private constructor

        @Override
        public byte[] serialize(String value) {
            return new byte[0];
        }

        @Override
        public String deserialize(byte[] data, SerializedFieldAttributes dataClass) {
            return "";
        }

        @Override
        public Class<String> getType() {
            return null;
        }

        @Override
        public String getDefaultValue() {
            return "";
        }
    }

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSerialize_NullObject() {
        ObjectSerializer serializer = new ObjectSerializer();
        byte[] result = serializer.serialize(null);
        assertNull(result, "Serialization of null should return null.");
    }

    @Test
    void testSerialize_NoAnnotatedFields() {
        class TestObject {
            private int field1;
            private String field2;
        }

        ObjectSerializer serializer = new ObjectSerializer();
        byte[] result = serializer.serialize(new TestObject());
        assertNotNull(result, "Result should not be null.");
        assertEquals(0, result.length, "Serialization of object with no annotated fields should return an empty byte array.");
    }

    @Test
    void testSerialize_AnnotatedFields() {
        class TestObject {
            @SerializedField(type=IntegerSerializer.class, identifier = (byte) 0x01)
            private final Integer field1 = 42;

            @SerializedField(type= StringSerializer.class, identifier = (byte) 0x01)
            private final String field2 = "Hello";
        }

        ObjectSerializer serializer = new ObjectSerializer();
        byte[] result = serializer.serialize(new TestObject());
        assertNotNull(result, "Serialized byte array should not be null.");
        byte[] expected = new byte[]{1, 42, 0, 0, 0, 1, 5, 0, 0, 0, 72, 101, 108, 108, 111};
        assertArrayEquals(expected, result, "Serialized byte array should match expected byte array.");
    }

    @Test
    void testSerialize_EmptyObject_HasIdentifierAndZeroLength() {
        class TestObject {
            @SerializedField(identifier = (byte) 0x01)
            private final Object objectField = new Object();
        }

        ObjectSerializer serializer = new ObjectSerializer();
        assertArrayEquals(serializer.serialize(new TestObject()), new byte[]{1, 0, 0, 0, 0});
    }

    @Test
    void testSerialize_NullString_ShouldReturnZeroLengthPrefix() {
        class TestObject {
            @SerializedField(type=IntegerSerializer.class, identifier = (byte) 0x01)
            private final Integer field1 = 0;

            @SerializedField(type=StringSerializer.class, identifier = (byte) 0x02)
            private final String field2 = null;
        }

        ObjectSerializer serializer = new ObjectSerializer();
        byte[] result = serializer.serialize(new TestObject());
        byte[] expected = new byte[]{1, 0, 0, 0, 0, 2, 0, 0, 0, 0};
        assertNotNull(result, "Serialized byte array should not be null.");
        assertEquals(expected.length, result.length, "Serialized byte array should match expected byte array length.");
        assertArrayEquals(expected, result, "Serialized byte array should match expected byte array.");
    }

    @Test
    void testGetSerializer_ValidClass() {
        try {
            ObjectSerializer objectSerializer = new ObjectSerializer();
            Method method = ObjectSerializer.class.getDeclaredMethod("getSerializer", Class.class);
            method.setAccessible(true);
            Object serializer = method.invoke(objectSerializer, DoubleSerializer.class);
            assertNotNull(serializer, "Serializer should not be null for a valid class.");
            assertInstanceOf(DoubleSerializer.class, serializer, "Serializer should be an instance of ValidSerializer.");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Method getSerializer(Class<?>) should be implemented.");
        }
    }

    @Test
    void testGetSerializer_InaccessibleConstructor() {
        try {
            ObjectSerializer objectSerializer = new ObjectSerializer();
            Method method = ObjectSerializer.class.getDeclaredMethod("getSerializer", Class.class);
            method.setAccessible(true);
            Exception exception = assertThrows(InvocationTargetException.class,
                    () -> method.invoke(objectSerializer, InaccessibleConstructorSerializer.class));

            assertInstanceOf(SerializerCreationException.class, exception.getCause());
            } catch (NoSuchMethodException e) {
                fail("Method getSerializer(Class<?>) should be implemented.");
        }
    }

    @Test
    void testGetSerializer_WithNullSerializer() {
        try {
            ObjectSerializer objectSerializer = new ObjectSerializer();
            Method method = ObjectSerializer.class.getDeclaredMethod("getSerializer", Class.class);
            method.setAccessible(true);
            Exception exception = assertThrows(InvocationTargetException.class,
                    () -> method.invoke(objectSerializer, (Object) null));

            assertInstanceOf(NullPointerException.class, exception.getCause());
        } catch (NoSuchMethodException e) {
            fail("Method getSerializer(Class<?>) should be implemented.");
        }
    }

    @Test
    void deserialize() {
    }

    @Test
    void getType() {
    }

    @Test
    void getDefaultValue() {
    }

    @Test
    void testDeserialize() {
    }
}