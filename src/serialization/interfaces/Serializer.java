package serialization.interfaces;

import serialization.dataclass.SerializedFieldAttributes;

public interface Serializer<T> {
    byte[] serialize(T value);
    default byte[] serialize(T value, SerializedFieldAttributes dataClass) {
        return serialize(value);
    }
    T deserialize(byte[] data, SerializedFieldAttributes dataClass);
    Class<T> getType();
    T getDefaultValue();
}

