package com.github.fmjsjx.entrepot.core;

import java.io.Closeable;
import java.nio.ByteBuffer;

import com.github.fmjsjx.entrepot.core.exception.AppendingException;

import io.netty.buffer.ByteBuf;

/**
 * Appends data.
 */
public interface Appender extends Closeable {

    @Override
    void close();

    boolean isOpen();

    int append(byte[] src) throws AppendingException;

    long append(byte[]... srcs) throws AppendingException;

    int append(ByteBuffer src) throws AppendingException;

    long append(ByteBuffer... srcs) throws AppendingException;

    int append(ByteBuf src) throws AppendingException;

    long append(ByteBuf... srcs) throws AppendingException;

    void force(boolean metaData);
}
