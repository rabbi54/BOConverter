package org.example.serialization.exceptions;

public class SerializerMismatchException extends Exception {
    public SerializerMismatchException() {
        super();
    }

    public SerializerMismatchException(String message) {
        super(message);
    }

    public SerializerMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerMismatchException(Throwable cause) {
        super(cause);
    }
}
