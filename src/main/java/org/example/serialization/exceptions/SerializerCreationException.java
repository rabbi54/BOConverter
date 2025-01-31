package org.example.serialization.exceptions;

public class SerializerCreationException extends Exception {
    public SerializerCreationException() {
        super();
    }

    public SerializerCreationException(String message) {
        super(message);
    }

    public SerializerCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerCreationException(Throwable cause) {
        super(cause);
    }
}
