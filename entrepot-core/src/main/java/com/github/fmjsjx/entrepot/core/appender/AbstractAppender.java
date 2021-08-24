package com.github.fmjsjx.entrepot.core.appender;

import com.github.fmjsjx.entrepot.core.Appender;
import com.github.fmjsjx.entrepot.core.EntrepotRuntimeException;
import com.github.fmjsjx.entrepot.core.exception.ClosedAppenderException;

public abstract class AbstractAppender implements Appender {

    protected volatile boolean closed;

    @Override
    public boolean isOpen() {
        return !closed;
    }

    protected void ensureOpen() {
        if (closed) {
            throw new ClosedAppenderException(this);
        }
    }

    @Override
    public void force(boolean metaData) {
        try {
            force0(metaData);
        } catch (Exception e) {
            throw new EntrepotRuntimeException("force updates failed", e);
        }
    }

    protected void force0(boolean metaData) throws Exception {
        // default do nothing
    }

    @Override
    public void close() {
        if (isOpen()) {
            synchronized (this) {
                if (!closed) {
                    closed = true;
                    try {
                        close0();
                    } catch (Exception e) {
                        throw new EntrepotRuntimeException("error occurs when close " + this, e);
                    }
                }
            }
        }
    }

    protected abstract void close0() throws Exception;

}
