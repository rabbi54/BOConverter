package utils.dataclass;

import utils.interfaces.ByteSerialize;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public record SerializationParameter(ByteSerialize byteSerialize, ByteBuffer buffer,
                                     AnnotationDataClass annotationDataClass, int length, Field field, Class<?> clazz, Object object, byte typeId) {

    public static class Builder {
        private ByteSerialize byteSerialize;
        private ByteBuffer buffer;
        private AnnotationDataClass annotationDataClass;
        private int length;
        private Field field;
        private Class<?> clazz;
        private Object object;
        private byte typeId;

        public Builder from(SerializationParameter dataClass) {
            this.byteSerialize = dataClass.byteSerialize();
            this.buffer = dataClass.buffer();
            this.annotationDataClass = dataClass.annotationDataClass();
            this.length = dataClass.length();
            this.field = dataClass.field();
            this.clazz = dataClass.clazz();
            this.object = dataClass.object();
            this.typeId = dataClass.typeId();
            return this;
        }

        public Builder byteSerialize(ByteSerialize byteSerialize) {
            this.byteSerialize = byteSerialize;
            return this;
        }

        public Builder buffer(ByteBuffer buffer) {
            this.buffer = buffer;
            return this;
        }

        public Builder annotationDataClass(AnnotationDataClass annotationDataClass) {
            this.annotationDataClass = annotationDataClass;
            return this;
        }

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder field(Field field) {
            this.field = field;
            return this;
        }

        public Builder clazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder object(Object object) {
            this.object = object;
            return this;
        }

        public Builder typeId(byte typeId) {
            this.typeId = typeId;
            return this;
        }

        public SerializationParameter build() {
            return new SerializationParameter(byteSerialize, buffer, annotationDataClass, length, field, clazz, object, typeId);
        }
    }
}