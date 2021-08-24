package com.github.fmjsjx.entrepot.core;

import java.util.concurrent.CompletableFuture;

import io.netty.buffer.ByteBuf;

public interface Wharf extends AutoCloseable {

    String name();

    CompletableFuture<Void> transferFrom(ByteBuf data);

    CompletableFuture<Void> transferFrom(ByteBuf... datas);

    CompletableFuture<Void> transferFrom(byte[] data);

    @Override
    void close();

}
