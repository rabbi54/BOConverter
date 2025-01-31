package boconverter;

import org.example.models.SleepBinning;
import org.junit.jupiter.api.Test;
import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.Serializer;
import org.example.serialization.serializers.ArraySerializer;
import org.example.serialization.serializers.DoubleSerializer;
import org.example.serialization.serializers.SleepBinningSerializer;
import org.example.serialization.serializers.TimeSerializer;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArraySerializerTest {

    @Test
    public void testDoubleArraySerialization() {

        Serializer<Double> innerSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(innerSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(ArrayList.class, (byte)0x12, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();
        doubleArray.add(1.23);
        doubleArray.add(4.56);
        doubleArray.add(7.89);

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(Long.class, (byte)(1), 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();
        timeArray.add(1629216000000L); // Example timestamp
        timeArray.add(1629302400000L); // Another timestamp

        byte[] serialized = timeArraySerializer.serialize(timeArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testEmptyDoubleArraySerialization() {
        DoubleSerializer doubleSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(doubleSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(Double.class, (byte)1, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testEmptyTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(Long.class, (byte)1, 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();

        byte[] serialized = timeArraySerializer.serialize(timeArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testSingleElementDoubleArraySerialization() {
        DoubleSerializer doubleSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(doubleSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(Double.class, (byte)1, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();
        doubleArray.add(42.42);

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testSingleElementTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(Long.class, (byte)1, 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();
        timeArray.add(1629216000000L);

        byte[] serialized = timeArraySerializer.serialize(timeArray, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testObjectArraySerializationNoObjectLengthPrefix() {
        SleepBinningSerializer sleepBinningSerializer = new SleepBinningSerializer();
        ArraySerializer<SleepBinning> sleepBinningArraySerializer = new ArraySerializer<>(sleepBinningSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(SleepBinningSerializer.class, (byte)1, 8, false);

        ArrayList<SleepBinning> binnings = new ArrayList<>();
        binnings.add(new SleepBinning(1, 2));
        binnings.add(new SleepBinning(3, 4));
        binnings.add(new SleepBinning(5, 6));

        byte[] serialized = sleepBinningArraySerializer.serialize(binnings, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<SleepBinning> deserialized = sleepBinningArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(binnings.size(), deserialized.size());
        for (int i = 0; i < binnings.size(); i++) {
            SleepBinning original = binnings.get(i);
            SleepBinning result = deserialized.get(i);

            assertEquals(original.getHrri(), result.getHrri());
            assertEquals(original.getHrss(), result.getHrss());
        }
    }

    @Test
    public void testObjectWhichSizeIsMoreThanTwoKB() {
        SleepBinningSerializer sleepBinningSerializer = new SleepBinningSerializer();
        ArraySerializer<SleepBinning> sleepBinningArraySerializer = new ArraySerializer<>(sleepBinningSerializer);
        SerializedFieldAttributes serializedFieldAttributes = new SerializedFieldAttributes(SleepBinningSerializer.class, (byte)1, 8, false);

        ArrayList<SleepBinning> binnings = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            binnings.add(new SleepBinning(i, i + 1));
        }

        byte[] serialized = sleepBinningArraySerializer.serialize(binnings, serializedFieldAttributes);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<SleepBinning> deserialized = sleepBinningArraySerializer.deserialize(withoutPrefix, serializedFieldAttributes);

        assertEquals(binnings.size(), deserialized.size());
        for (int i = 0; i < binnings.size(); i++) {
            SleepBinning original = binnings.get(i);
            SleepBinning result = deserialized.get(i);

            assertEquals(original.getHrri(), result.getHrri());
            assertEquals(original.getHrss(), result.getHrss());
        }
    }
}

