package utils.serializers;

public class ByteSerializerDataClass {
    byte[] data;
    Class<?> type;
    byte identifier;
    int length;

    public ByteSerializerDataClass(byte[] data, Class<?> type, byte identifier, int length) {
        this.data = data;
        this.type = type;
        this.identifier = identifier;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public byte getIdentifier() {
        return identifier;
    }

    public void setIdentifier(byte identifier) {
        this.identifier = identifier;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
