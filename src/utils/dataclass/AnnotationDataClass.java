package utils.dataclass;

public class AnnotationDataClass {
    public Class<?> type;
    public byte identifier;
    public int length;

    public AnnotationDataClass(Class<?> type, byte identifier, int length) {
        this.type = type;
        this.identifier = identifier;
        this.length = length;
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
