package utils.interfaces;

import utils.dataclass.AnnotationDataClass;

public interface Serializer<T> {
    byte[] serialize(T value);
    default byte[] serialize(T value, AnnotationDataClass dataClass) {
        return serialize(value);
    }
    T deserialize(byte[] data, AnnotationDataClass dataClass);
    Class<T> getType();
}

