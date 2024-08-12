package utils.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ByteSerialize {
    Class<?> type() default Object.class;
    byte identifier();
    int length() default 0;
    boolean required() default true;
    Class<?> innerType() default Object.class;
}

