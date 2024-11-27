package serialization.utils;

import serialization.dataclass.SerializationParameter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public void setFieldValue(SerializationParameter parameterBuilder, Field field, Object deserializedValue) throws IllegalAccessException {
        field.setAccessible(true);
        Method setterMethod;
        try {
            setterMethod = getFieldSetterMethod(field);
            setterMethod.invoke(parameterBuilder.object(), deserializedValue);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            field.set(parameterBuilder.object(), deserializedValue);
        }
    }

    public Object getFieldValue(Field field, Object object) {
        try {
            Method getterMethod = getFieldGetterMethod(field);
            return getterMethod.invoke(object);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return getFieldValueDirectly(field, object);
        }
    }

    private Object getFieldValueDirectly(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Method getFieldGetterMethod(Field field) throws NoSuchMethodException {
        String fieldName = field.getName();
        String getterMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return field.getDeclaringClass().getMethod(getterMethodName);
    }

    private Method getFieldSetterMethod(Field field) throws NoSuchMethodException {
        String fieldName = field.getName();
        String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return field.getDeclaringClass().getMethod(setterMethodName, field.getType());
    }

}
