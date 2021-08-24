package com.github.fmjsjx.entrepot.core.exception;

import com.github.fmjsjx.entrepot.core.EntrepotRuntimeException;

public class AppendingException extends EntrepotRuntimeException {

    private static final long serialVersionUID = 1L;

    public AppendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppendingException(Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
    }

}
