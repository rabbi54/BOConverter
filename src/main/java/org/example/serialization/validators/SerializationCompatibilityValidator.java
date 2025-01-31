package org.example.serialization.validators;

import org.example.models.SleepBinning;
import org.jetbrains.annotations.NotNull;
import org.example.serialization.exceptions.SerializerMismatchException;
import org.example.serialization.serializers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SerializationCompatibilityValidator {
    private static final Map<Class<?>, Class<?>> serializerFieldCompatibilityMap = new HashMap<>();
    static {
        serializerFieldCompatibilityMap.put(IntegerSerializer.class, Integer.class);
        serializerFieldCompatibilityMap.put(UUIDSerializer.class, String.class);
        serializerFieldCompatibilityMap.put(StringSerializer.class, String.class);
        serializerFieldCompatibilityMap.put(DoubleSerializer.class, Double.class);
        serializerFieldCompatibilityMap.put(ArraySerializer.class, ArrayList.class);
        serializerFieldCompatibilityMap.put(BooleanSerializer.class, Boolean.class);
        serializerFieldCompatibilityMap.put(ByteIntSerializer.class, Integer.class);
        serializerFieldCompatibilityMap.put(ShortSerializer.class, Short.class);
        serializerFieldCompatibilityMap.put(FloatSerializer.class, Float.class);
        serializerFieldCompatibilityMap.put(LongSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(LongFrom4ByteSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(LocationDataSerializer.class, Double.class);
        serializerFieldCompatibilityMap.put(TimeSerializer.class, Long.class);
        serializerFieldCompatibilityMap.put(SleepBinningSerializer.class, SleepBinning.class);

    }

    public static void checkSerializerFieldCompatibility(@NotNull Class<?> serializerClass, @NotNull Class<?> fieldClass) throws SerializerMismatchException {
        Class<?> compatibleClass = serializerFieldCompatibilityMap.get(serializerClass);
        if (compatibleClass == null) {
            throw new SerializerMismatchException(serializerClass.getName() + " serializer has no corresponding compatibility for class " + fieldClass.getName());
        }
        if (compatibleClass != fieldClass) {
            throw new SerializerMismatchException(serializerClass.getName() + " has no compatibility for class " + fieldClass.getName());
        }
    }

    public static void checkSupportedSerializer(@NotNull Class<?> clazz) {
        if (clazz == Object.class ) {
            return;
        }
        if (!serializerFieldCompatibilityMap.containsKey(clazz)) {
            throw new IllegalArgumentException("No serializer registered for type : " + clazz.getName());
        }
    }
}
