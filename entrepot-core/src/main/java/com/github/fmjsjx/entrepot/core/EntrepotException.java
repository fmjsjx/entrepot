package com.github.fmjsjx.entrepot.core;

public class EntrepotException extends Exception {

    private static final long serialVersionUID = 1L;

    public EntrepotException() {
        super();
    }

    public EntrepotException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntrepotException(String message) {
        super(message);
    }

    public EntrepotException(Throwable cause) {
        super(cause);
    }

}
