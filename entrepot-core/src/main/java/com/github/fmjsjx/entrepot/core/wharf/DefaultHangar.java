package com.github.fmjsjx.entrepot.core.wharf;

import static com.github.fmjsjx.entrepot.core.Constants.LF;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.fmjsjx.entrepot.core.Appender;
import com.github.fmjsjx.entrepot.core.Wharf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
public class DefaultHangar implements Wharf {

    private static final IllegalArgumentException emptyArrayException = new IllegalArgumentException(
            "length of datas must greater than 0");

    public static final int NEVER = 0;
    public static final int AUTO = 1;
    public static final int ALWAYS = 2;

    @ToString.Include
    private final String name;
    @ToString.Include
    private final Appender appender;
    @ToString.Include
    private final ExecutorService executor;
    @ToString.Include
    private final int autoLineFeed;

    private final ByteBuf lineFeedBuf;
    private final ByteBuffer lineFeedBuffer;

    @ToString.Include
    private final long forcePeriodMillis;
    private final Timer timer;
    private final TimerTask forceTask;

    private long lastForceMillis;

    private Timeout timeout;

    public DefaultHangar(String name, Appender appender) {
        this(name, appender, null, null);
    }

    public DefaultHangar(String name, Appender appender, Duration forcePeriod, Timer timer) {
        this(name, appender, null, AUTO, forcePeriod, timer);
    }

    public DefaultHangar(String name, Appender appender, ExecutorService executor, int autoLineFeed) {
        this(name, appender, executor, autoLineFeed, null, null);
    }

    public DefaultHangar(String name, Appender appender, ExecutorService executor, int autoLineFeed,
            Duration forcePeriod, Timer timer) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.appender = Objects.requireNonNull(appender, "appender must not be null");
        this.executor = executor != null ? executor
                : Executors.newSingleThreadExecutor(new DefaultThreadFactory("hangar-" + name));
        this.autoLineFeed = autoLineFeed;
        if (autoLineFeed > 0) {
            var buf = UnpooledByteBufAllocator.DEFAULT.buffer(1, 1).writeByte(LF);
            lineFeedBuf = Unpooled.unreleasableBuffer(buf.asReadOnly()).markReaderIndex();
            lineFeedBuffer = buf.internalNioBuffer(0, 1).mark();
        } else {
            lineFeedBuf = null;
            lineFeedBuffer = null;
        }
        if (forcePeriod != null) {
            this.forcePeriodMillis = forcePeriod.toMillis();
            this.timer = Objects.requireNonNull(timer, "timer must not be null when forcePeriod exists");
            this.forceTask = forceTask();
            initForceTask();
        } else {
            this.forcePeriodMillis = 0;
            this.timer = timer;
            this.forceTask = null;
        }
    }

    @Override
    public String name() {
        return name;
    }

    private void initForceTask() {
        lastForceMillis = System.currentTimeMillis();
        timeout = timer.newTimeout(forceTask, forcePeriodMillis, TimeUnit.MILLISECONDS);
    }

    private TimerTask forceTask() {
        return t -> {
            if (timeout == t && !t.isCancelled()) {
                executor.execute(this::force0);
            }
        };
    }

    private void force0() {
        force0(System.currentTimeMillis());
    }

    private void force0(long timeMillis) {
        var checkTime = lastForceMillis + forcePeriodMillis;
        if (checkTime <= timeMillis) {
            appender.force(false);
            lastForceMillis = timeMillis;
            timeout = timer.newTimeout(forceTask, forcePeriodMillis, TimeUnit.MILLISECONDS);
        } else {
            timeout = timer.newTimeout(forceTask, checkTime - timeMillis, TimeUnit.MILLISECONDS);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isAutoLineFeed() {
        return autoLineFeed == AUTO;
    }

    public boolean isAlwaysLineFeed() {
        return autoLineFeed == ALWAYS;
    }

    public int autoLineFeed() {
        return autoLineFeed;
    }

    @Override
    public CompletableFuture<Void> transferFrom(ByteBuf data) {
        if (isAlwaysLineFeed() || (isAutoLineFeed() && data.getByte(data.writerIndex() - 1) != LF)) {
            return CompletableFuture.runAsync(() -> appendWithLineFeed(data), executor);
        } else {
            return CompletableFuture.runAsync(() -> append(data), executor);
        }
    }

    private void appendWithLineFeed(ByteBuf data) {
        try {
            appender.append(data, lineFeedBuf.resetReaderIndex());
            force0();
        } finally {
            ReferenceCountUtil.safeRelease(data);
        }
    }

    private void append(ByteBuf data) {
        try {
            appender.append(data);
            force0();
        } finally {
            ReferenceCountUtil.safeRelease(data);
        }
    }

    @Override
    public CompletableFuture<Void> transferFrom(ByteBuf... datas) {
        if (datas.length == 0) {
            return CompletableFuture.failedFuture(emptyArrayException);
        }
        if (isAlwaysLineFeed()) {
            return CompletableFuture.runAsync(() -> appendWithAlwaysLineFeed(datas), executor);
        } else if (isAutoLineFeed()) {
            return CompletableFuture.runAsync(() -> appendWithAutoLineFeed(datas), executor);
        } else {
            return CompletableFuture.runAsync(() -> append(datas), executor);
        }
    }

    private void appendWithAlwaysLineFeed(ByteBuf[] datas) {
        try {
            var lineFeedBuf = this.lineFeedBuf.resetReaderIndex();
            ByteBuf[] srcs = new ByteBuf[datas.length * 2];
            for (int i = 0; i < datas.length; i++) {
                int index = i * 2;
                srcs[index] = datas[i];
                srcs[index + 1] = lineFeedBuf.duplicate();
            }
            appender.append(srcs);
            force0();
        } finally {
            for (ByteBuf data : datas) {
                ReferenceCountUtil.safeRelease(data);
            }
        }
    }

    private void appendWithAutoLineFeed(ByteBuf[] datas) {
        try {
            ArrayList<ByteBuf> list = new ArrayList<>(datas.length * 2);
            var lineFeedBuf = this.lineFeedBuf.resetReaderIndex();
            for (ByteBuf data : datas) {
                list.add(data);
                if (data.getByte(data.writerIndex() - 1) != LF) {
                    list.add(lineFeedBuf.duplicate());
                }
            }
            appender.append(list.toArray(new ByteBuf[list.size()]));
            force0();
        } finally {
            for (ByteBuf data : datas) {
                ReferenceCountUtil.safeRelease(data);
            }
        }
    }

    private void append(ByteBuf[] datas) {
        try {
            appender.append(datas);
            force0();
        } finally {
            for (ByteBuf data : datas) {
                ReferenceCountUtil.safeRelease(data);
            }
        }
    }

    @Override
    public CompletableFuture<Void> transferFrom(byte[] data) {
        if (isAutoLineFeed() && data[data.length - 1] != LF) {
            return CompletableFuture.runAsync(() -> appendWithLineFeed(ByteBuffer.wrap(data)), executor);
        } else {
            return CompletableFuture.runAsync(() -> append(data), executor);
        }
    }

    private void appendWithLineFeed(ByteBuffer buffer0) {
        var buffer1 = lineFeedBuffer.reset();
        appender.append(buffer0, buffer1);
        force0();
    }

    private void append(byte[] data) {
        appender.append(data);
        force0();
    }

    @Override
    public void close() {
        if (timeout != null) {
            timeout.cancel();
        }
        executor.shutdown();
        if (lineFeedBuf != null) {
            lineFeedBuf.release();
        }
    }

}
