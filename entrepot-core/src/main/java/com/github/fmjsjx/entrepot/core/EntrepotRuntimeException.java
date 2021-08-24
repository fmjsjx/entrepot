package com.github.fmjsjx.entrepot.core;

public class EntrepotRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntrepotRuntimeException() {
        super();
    }

    public EntrepotRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntrepotRuntimeException(String message) {
        super(message);
    }

    public EntrepotRuntimeException(Throwable cause) {
        super(cause);
    }

}
