package com.github.fmjsjx.entrepot.core.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.ToString;
import lombok.ToString.Include;
import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link FileAppendWriter} implementation with write data to a
 * {@link FileChannel}.
 * 
 * <p>
 * All methods provides by this class is Non thread safe.
 * </p>
 * 
 * @see FileAppendWriter
 * @see FileChannel
 */
@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class DefaultFileAppendWriter implements FileAppendWriter {

    private static final ClosedChannelException closedChannelException = new ClosedChannelException();

    @Include
    private final File f;
    private final RandomAccessFile raf;
    private final FileChannel file;
    @Include
    private final AtomicBoolean closed = new AtomicBoolean();
    @Include
    private long position;

    /**
     * Create a new instance using the given {@link Path}.
     * 
     * @param path the {@link Path} of the {@link File} which to be written
     * 
     * @throws IOException if an I/O error occurs
     */
    public DefaultFileAppendWriter(Path path) throws IOException {
        this(path.toFile());
    }

    /**
     * Create a new instance using the given pathname.
     * 
     * @param pathname the path name string of the {@link File} which to be written
     * 
     * @throws IOException if an I/O error occurs
     */
    public DefaultFileAppendWriter(String pathname) throws IOException {
        this(new File(pathname));
    }

    /**
     * Create a new instance using the given {@link URI}.
     * 
     * @param uri the {@link URI} of the {@link File} which to be written
     * 
     * @throws IOException if an I/O error occurs
     */
    public DefaultFileAppendWriter(URI uri) throws IOException {
        this(new File(uri));
    }

    /**
     * Create a new instance using the given {@link File}.
     * 
     * @param f the {@link File} which to be written
     * 
     * @throws IOException if an I/O error occurs
     */
    public DefaultFileAppendWriter(File f) throws IOException {
        this.f = f;
        this.raf = new RandomAccessFile(f, "rw");
        this.file = raf.getChannel();
        ensureAppend();
    }

    /**
     * Create a new instance.
     * 
     * @param file the {@link FileChannel} which used to write data
     * 
     * @throws IOException if an I/O error occurs
     */
    public DefaultFileAppendWriter(FileChannel file) throws IOException {
        this.f = null;
        this.raf = null;
        this.file = file;
        ensureAppend();
    }

    private void ensureAppend() throws IOException {
        log.debug("Set position to the end of the file {}", this);
        var fileSize = file.size();
        var pos = position;
        if (pos != fileSize) {
            position = fileSize;
            file.position(fileSize);
        }
    }

    private void rollback() {
        if (isOpen()) {
            try {
                var fc = file;
                var fileSize = fc.size();
                var pos = position;
                if (fileSize > pos) {
                    fc.position(pos);
                    fc.truncate(pos);
                }
            } catch (IOException e) {
                log.error("Rollback failed on {}", this, e);
            }
        }
    }

    @Override
    public int write(byte[] src) throws IOException {
        return write(Unpooled.wrappedBuffer(src));
    }

    @Override
    public long write(byte[]... srcs) throws IOException {
        if (srcs.length == 0) {
            return 0;
        }
        var wrappers = Arrays.stream(srcs).map(ByteBuffer::wrap).toArray(ByteBuffer[]::new);
        return write(wrappers);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        var len = write0(src);
        position += len;
        return len;
    }

    private int write0(ByteBuffer src) throws IOException {
        try {
            return file.write(src);
        } catch (IOException e) {
            rollback();
            throw e;
        }
    }

    @Override
    public long write(ByteBuffer... srcs) throws IOException {
        if (srcs.length == 0) {
            return 0;
        }
        var len = write0(srcs);
        position += len;
        return len;
    }

    private long write0(ByteBuffer... srcs) throws IOException {
        try {
            return file.write(srcs);
        } catch (IOException e) {
            rollback();
            throw e;
        }
    }

    @Override
    public int write(ByteBuf src) throws IOException {
        var len = write0(src);
        position += len;
        return len;
    }

    private int write0(ByteBuf src) throws IOException {
        try {
            return FileChannelUtil.write(file, src);
        } catch (IOException e) {
            rollback();
            throw e;
        }
    }

    @Override
    public long write(ByteBuf... srcs) throws IOException {
        if (srcs.length == 0) {
            return 0;
        }
        var len = write0(srcs);
        position += len;
        return len;
    }

    private long write0(ByteBuf... srcs) throws IOException {
        try {
            return FileChannelUtil.write(file, srcs);
        } catch (IOException e) {
            rollback();
            throw e;
        }
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            if (raf != null) {
                raf.close();
            } else if (file != null) {
                file.close();
            }
        }
    }

    @Override
    public void force(boolean metaData) throws IOException {
        file.force(metaData);
    }

    @Override
    public long position() throws IOException {
        ensureOpen();
        return position;
    }

    private void ensureOpen() throws ClosedChannelException {
        if (closed.get()) {
            throw closedChannelException;
        }
    }

    @Override
    public long size() throws IOException {
        return file.size();
    }

}
