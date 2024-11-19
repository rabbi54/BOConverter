package test.java;

import models.SleepBinning;
import org.junit.jupiter.api.Test;
import utils.dataclass.AnnotationDataClass;
import utils.interfaces.Serializer;
import utils.serializers.ArraySerializer;
import utils.serializers.DoubleSerializer;
import utils.serializers.SleepBinningSerializer;
import utils.serializers.TimeSerializer;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ArraySerializerTest {

    @Test
    public void testDoubleArraySerialization() {

        Serializer<Double> innerSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(innerSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(ArrayList.class, (byte)0x12, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();
        doubleArray.add(1.23);
        doubleArray.add(4.56);
        doubleArray.add(7.89);

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(Long.class, (byte)(1), 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();
        timeArray.add(1629216000000L); // Example timestamp
        timeArray.add(1629302400000L); // Another timestamp

        byte[] serialized = timeArraySerializer.serialize(timeArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testEmptyDoubleArraySerialization() {
        DoubleSerializer doubleSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(doubleSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(Double.class, (byte)1, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testEmptyTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(Long.class, (byte)1, 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();

        byte[] serialized = timeArraySerializer.serialize(timeArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testSingleElementDoubleArraySerialization() {
        DoubleSerializer doubleSerializer = new DoubleSerializer();
        ArraySerializer<Double> doubleArraySerializer = new ArraySerializer<>(doubleSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(Double.class, (byte)1, 8, false);

        ArrayList<Double> doubleArray = new ArrayList<>();
        doubleArray.add(42.42);

        byte[] serialized = doubleArraySerializer.serialize(doubleArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Double> deserialized = doubleArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(doubleArray, deserialized);
    }

    @Test
    public void testSingleElementTimeArraySerialization() {
        TimeSerializer timeSerializer = new TimeSerializer();
        ArraySerializer<Long> timeArraySerializer = new ArraySerializer<>(timeSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(Long.class, (byte)1, 4, false);

        ArrayList<Long> timeArray = new ArrayList<>();
        timeArray.add(1629216000000L);

        byte[] serialized = timeArraySerializer.serialize(timeArray, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<Long> deserialized = timeArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(timeArray, deserialized);
    }

    @Test
    public void testObjectArraySerializationNoObjectLengthPrefix() {
        SleepBinningSerializer sleepBinningSerializer = new SleepBinningSerializer();
        ArraySerializer<SleepBinning> sleepBinningArraySerializer = new ArraySerializer<>(sleepBinningSerializer);
        AnnotationDataClass annotationDataClass = new AnnotationDataClass(SleepBinningSerializer.class, (byte)1, 8, false);

        ArrayList<SleepBinning> binnings = new ArrayList<>();
        binnings.add(new SleepBinning(1, 2));
        binnings.add(new SleepBinning(3, 4));
        binnings.add(new SleepBinning(5, 6));

        byte[] serialized = sleepBinningArraySerializer.serialize(binnings, annotationDataClass);
        byte[] withoutPrefix = Arrays.copyOfRange(serialized, 4, serialized.length);
        ArrayList<SleepBinning> deserialized = sleepBinningArraySerializer.deserialize(withoutPrefix, annotationDataClass);

        assertEquals(binnings.size(), deserialized.size());
        for (int i = 0; i < binnings.size(); i++) {
            SleepBinning original = binnings.get(i);
            SleepBinning result = deserialized.get(i);

            assertEquals(original.getHrri(), result.getHrri());
            assertEquals(original.getHrss(), result.getHrss());
        }
    }
}

