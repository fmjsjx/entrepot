package com.github.fmjsjx.entrepot.core.appender;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.fmjsjx.entrepot.core.EntrepotRuntimeException;
import com.github.fmjsjx.entrepot.core.appender.RollingPolicy.TriggeringPhase;
import com.github.fmjsjx.entrepot.core.exception.AppendingException;
import com.github.fmjsjx.entrepot.core.nio.DefaultFileAppendWriter;
import com.github.fmjsjx.entrepot.core.nio.FileAppendWriter;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollingFileAppender extends AbstractAppender {

    private final Path parentPath;
    private final RollingPolicy policy;

    private final File parentDir;

    private FileAppendWriter writer;

    public RollingFileAppender(String parentPath, RollingPolicy policy) {
        this(Paths.get(parentPath), policy);
    }

    public RollingFileAppender(URI parentUri, RollingPolicy policy) {
        this(Paths.get(parentUri), policy);
    }

    public RollingFileAppender(Path parentPath, RollingPolicy policy) {
        this(parentPath, parentPath.toFile(), policy);
    }

    RollingFileAppender(Path parentPath, File parentDir, RollingPolicy policy) {
        this.parentPath = parentPath;
        this.parentDir = parentDir;
        this.policy = policy;
        init();
    }

    private void init() {
        log.debug("Init {}", this);
        if (parentDir.exists()) {
            if (!parentDir.isDirectory()) {
                throw new EntrepotRuntimeException(parentPath + " must be a directory");
            }
        } else {
            parentDir.mkdirs();
            log.debug("Create parent dir {}", parentDir);
        }
        policy.update(0);
        CHK: for (;;) {
            var file = new File(parentDir, policy.namespace());
            log.debug("Check file {}", file);
            if (file.exists()) {
                var size = file.length();
                if (policy.update(size)) {
                    continue CHK;
                }
            }
            log.debug("Create file append writer on {}", file);
            try {
                writer = new DefaultFileAppendWriter(file);
            } catch (IOException e) {
                throw new EntrepotRuntimeException("create file append writer failed on " + file, e);
            }
            log.debug("Created writer: {}", writer);
            break CHK;
        }
    }

    public Path getParentPath() {
        return parentPath;
    }

    public Class<? extends RollingPolicy> getPolicyClass() {
        return policy.getClass();
    }

    @Override
    public int append(byte[] src) throws AppendingException {
        return appendOne(src, appendHeapArray);
    }

    private <E> int appendOne(E data, AppendOneAction<E> action) throws AppendingException {
        try {
            checkRolling(TriggeringPhase.BEFORE_APPEND);
            var len = action.apply(writer, data);
            checkRolling(TriggeringPhase.AFTER_APPEND);
            return len;
        } catch (AppendingException e) {
            throw e;
        } catch (Exception e) {
            throw new AppendingException(e);
        }
    }

    private void checkRolling(TriggeringPhase phase) throws IOException {
        if (policy.canTriggered(phase)) {
            policy.tryUpdate(writer.position()).ifPresent(this::rollNext);
        }
    }

    private <E> long appendMany(E data, AppendManyAction<E> action) throws AppendingException {
        try {
            checkRolling(TriggeringPhase.BEFORE_APPEND);
            var len = action.apply(writer, data);
            checkRolling(TriggeringPhase.AFTER_APPEND);
            return len;
        } catch (AppendingException e) {
            throw e;
        } catch (Exception e) {
            throw new AppendingException(e);
        }
    }

    private void rollNext(String namespace) {
        try {
            rollNext0(namespace);
        } catch (IOException e) {
            throw new AppendingException(e);
        }
    }

    private void rollNext0(String namespace) throws IOException {
        force0(true);
        writer.close();
        var file = new File(parentDir, namespace);
        log.debug("Next rolling file: {}", file);
        if (namespace.contains("/")) { // has path separator
            log.debug("Ensure parent dir for file: {}", file);
            file.getParentFile().mkdirs();
        }
        writer = new DefaultFileAppendWriter(file);
        log.debug("Created writer: {}", writer);
    }

    @Override
    public long append(byte[]... srcs) throws AppendingException {
        return appendMany(srcs, appendHeapArrays);
    }

    @Override
    public int append(ByteBuffer src) throws AppendingException {
        return appendOne(src, appendBuffer);
    }

    @Override
    public long append(ByteBuffer... srcs) throws AppendingException {
        return appendMany(srcs, appendBuffers);
    }

    @Override
    public int append(ByteBuf src) throws AppendingException {
        return appendOne(src, appendBuf);
    }

    @Override
    public long append(ByteBuf... srcs) throws AppendingException {
        return appendMany(srcs, appendBufs);
    }

    @Override
    protected void force0(boolean metaData) throws IOException {
        writer.force(metaData);
    }

    @Override
    protected void close0() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public String toString() {
        return "RollingFileAppender(open=" + isOpen() + ", parentPath=" + parentPath + ", policy=" + policy + ")";
    }

    @FunctionalInterface
    private interface AppendOneAction<E> {

        int apply(FileAppendWriter writer, E data) throws IOException;

    }

    @FunctionalInterface
    private interface AppendManyAction<E> {

        long apply(FileAppendWriter writer, E data) throws IOException;

    }

    private static final AppendOneAction<byte[]> appendHeapArray = FileAppendWriter::write;
    private static final AppendManyAction<byte[][]> appendHeapArrays = FileAppendWriter::write;
    private static final AppendOneAction<ByteBuffer> appendBuffer = FileAppendWriter::write;
    private static final AppendManyAction<ByteBuffer[]> appendBuffers = FileAppendWriter::write;
    private static final AppendOneAction<ByteBuf> appendBuf = FileAppendWriter::write;
    private static final AppendManyAction<ByteBuf[]> appendBufs = FileAppendWriter::write;

}
