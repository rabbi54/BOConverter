package org.example.serialization.managers;

import org.example.serialization.dataclass.SerializedFieldAttributes;
import org.example.serialization.interfaces.SerializedField;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SerializedFieldManager {
    public SerializedField getSerializedFieldByIdentifier(byte type, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedField.class)) {
                continue;
            }
            field.setAccessible(true);
            SerializedField annotation = field.getAnnotation(SerializedField.class);
            if (annotation != null && annotation.identifier() == type) {
                return annotation;
            }
        }
        return null;
    }

    public Field findFieldByIdentifier(Class<?> clazz, byte type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedField.class)) {
                continue;
            }
            SerializedField annotation = field.getAnnotation(SerializedField.class);
            if (annotation.identifier() == type) {
                return field;
            }
        }
        return null;
    }

    public Class<?> getInnerClass(Field field) throws Exception {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            return (Class<?>) typeArguments[0];
        }
        throw new Exception(field.getName() + " is not a parameterized type");
    }

    public SerializedFieldAttributes getSerializedFieldAttributes(SerializedField annotation) {
        return new SerializedFieldAttributes(
                annotation.type(),
                annotation.identifier(),
                annotation.length(),
                annotation.required()
        );
    }

}
