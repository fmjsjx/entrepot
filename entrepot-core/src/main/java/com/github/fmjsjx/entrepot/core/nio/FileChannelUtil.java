package com.github.fmjsjx.entrepot.core.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FileChannelUtil {

    static final int write(FileChannel dest, ByteBuf src) throws IOException {
        return src.readBytes(dest, src.readableBytes());
    }

    static final long write(FileChannel dest, ByteBuf... srcs) throws IOException {
        long sum = 0;
        for (ByteBuf src : srcs) {
            sum += write(dest, src);
        }
        return sum;
    }

}
