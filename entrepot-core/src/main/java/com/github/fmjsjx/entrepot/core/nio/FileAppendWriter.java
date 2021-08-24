package com.github.fmjsjx.entrepot.core.nio;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

/**
 * A writer that can only write file in append mode.
 * 
 * @see Closeable
 */
public interface FileAppendWriter extends Closeable {

    /**
     * Writes <code>src.length</code> bytes from the specified byte array to the
     * file.
     * 
     * @param src The data.
     * 
     * @return The number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     */
    int write(byte[] src) throws IOException;

    /**
     * Writes a sequence of bytes to the file from the given byte arrays.
     * 
     * @param srcs The byte arrays from which bytes are to be retrieved
     * 
     * @return The number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     */
    long write(byte[]... srcs) throws IOException;

    /**
     * Writes a sequence of bytes to the file from the given buffer.
     * 
     * @param src The buffer from which bytes are to be retrieved
     * 
     * @return the number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     */
    int write(ByteBuffer src) throws IOException;

    /**
     * Writes a sequence of bytes to the file from the given buffers.
     * 
     * @param srcs The buffers from which bytes are to be retrieved
     * 
     * @return The number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     */
    long write(ByteBuffer... srcs) throws IOException;

    /**
     * Writes a sequence of bytes to the file from the given buffer.
     * 
     * @param src The buffer from which bytes are to be retrieved
     * 
     * @return The number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     * 
     * @see {@link ByteBuf}
     */
    int write(ByteBuf src) throws IOException;

    /**
     * Writes a sequence of bytes to the file from the given buffers.
     * 
     * @param srcs The buffers from which bytes are to be retrieved
     * 
     * @return The number of bytes written, possibly zero
     * 
     * @throws IOException if an I/O error occurs
     * 
     * @see {@link ByteBuf}
     */
    long write(ByteBuf... srcs) throws IOException;

    /**
     * Tells whether or not this writer is open.
     *
     * @return {@code true} if, and only if, this writer is open
     */
    public boolean isOpen();

    /**
     * Closes this writer and releases any system resources associated with this
     * writer.
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Forces any updates to this writer's file to be written to the storage device
     * that contains it.
     * 
     * @param metaData If {@code true} then this method is required to force changes
     *                 to both the file's content and metadata to be written to
     *                 storage; otherwise, it need only force content changes to be
     *                 written
     *
     * @throws IOException if an I/O error occurs
     */
    void force(boolean metaData) throws IOException;

    /**
     * Returns this writer's file position.
     * 
     * <p>
     * Because {@link FileAppendWriter} always do appending operations, the file
     * position should and <b>MUST</b> always equals to file size.
     * </p>
     *
     * @return This writer's file position, a non-negative integer counting the
     *         number of bytes from the beginning of the file to the current
     *         position
     *
     * @throws IOException if an I/O error occurs
     */
    long position() throws IOException;

    /**
     * Returns the current size of this writer's file.
     *
     * @return The current size of this writer's file, measured in bytes
     *
     * @throws IOException If some other I/O error occurs
     */
    long size() throws IOException;

}
