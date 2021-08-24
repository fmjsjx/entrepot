package com.github.fmjsjx.entrepot.core.exception;

import com.github.fmjsjx.entrepot.core.Appender;
import com.github.fmjsjx.entrepot.core.EntrepotRuntimeException;

public class ClosedAppenderException extends EntrepotRuntimeException {

    private static final long serialVersionUID = 1L;

    public ClosedAppenderException(Appender appender) {
        super(appender.toString());
    }

}
