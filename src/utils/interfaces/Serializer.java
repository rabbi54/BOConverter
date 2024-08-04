package utils.interfaces;

import utils.serializers.ByteSerializerDataClass;

public interface Serializer<T> {
    byte[] serialize(T value);
    T deserialize(ByteSerializerDataClass byteSerializerDataClass);
    Class<T> getType();
}

