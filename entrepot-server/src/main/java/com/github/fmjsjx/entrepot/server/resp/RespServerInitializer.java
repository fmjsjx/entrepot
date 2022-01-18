package com.github.fmjsjx.entrepot.server.resp;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.github.fmjsjx.entrepot.server.util.RespUtil;
import com.github.fmjsjx.libnetty.handler.ssl.ChannelSslInitializer;
import com.github.fmjsjx.libnetty.resp.RedisRequest;
import com.github.fmjsjx.libnetty.resp.RedisRequestDecoder;
import com.github.fmjsjx.libnetty.resp.RespMessageEncoder;
import com.github.fmjsjx.libnetty.resp.util.IgnoredCaseAsciiKeyMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class RespServerInitializer extends ChannelInitializer<SocketChannel> {

    static final RespMessageEncoder respMessageEncoder = new RespMessageEncoder();

    private final int timeoutSeconds;
    private final boolean sslEnabled;
    private final ChannelSslInitializer<Channel> channelSslInitializer;
    private final IgnoredCaseAsciiKeyMap<BiConsumer<ChannelHandlerContext, RedisRequest>> commandProcedures;

    public RespServerInitializer(int timeoutSeconds, Optional<ChannelSslInitializer<Channel>> channelSslInitializer,
            Map<String, BiConsumer<ChannelHandlerContext, RedisRequest>> commands) {
        this.timeoutSeconds = timeoutSeconds;
        this.sslEnabled = channelSslInitializer.isPresent();
        this.channelSslInitializer = channelSslInitializer.orElse(null);
        var commandProcedures = this.commandProcedures = new IgnoredCaseAsciiKeyMap<>();
        commandProcedures.put("PING", RespUtil.ping());
        commandProcedures.put("SELECT", RespUtil.alwaysOk());
        commandProcedures.put("AUTH", RespUtil.alwaysOk());
        commandProcedures.put("QUIT", RespUtil.quit());
        commands.forEach(commandProcedures::put);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        var pipeline = ch.pipeline();
        int timeoutSeconds = this.timeoutSeconds;
        if (timeoutSeconds > 0) {
            pipeline.addLast(new ReadTimeoutHandler(timeoutSeconds));
        }
        if (sslEnabled) {
            channelSslInitializer.init(ch);
        }
        pipeline.addLast(respMessageEncoder);
        pipeline.addLast(new RedisRequestDecoder());
        pipeline.addLast(new RespServerHandler(commandProcedures));
    }

}
