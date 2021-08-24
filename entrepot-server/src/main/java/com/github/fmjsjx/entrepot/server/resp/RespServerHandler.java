package com.github.fmjsjx.entrepot.server.resp;

import java.util.function.BiConsumer;

import com.github.fmjsjx.entrepot.server.util.RespUtil;
import com.github.fmjsjx.libnetty.resp.RedisRequest;
import com.github.fmjsjx.libnetty.resp.RespMessages;
import com.github.fmjsjx.libnetty.resp.util.IgnoredCaseAsciiKeyMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RespServerHandler extends SimpleChannelInboundHandler<RedisRequest> {

    private final IgnoredCaseAsciiKeyMap<BiConsumer<ChannelHandlerContext, RedisRequest>> commandProcedures;

    public RespServerHandler(
            IgnoredCaseAsciiKeyMap<BiConsumer<ChannelHandlerContext, RedisRequest>> commandProcedures) {
        this.commandProcedures = commandProcedures;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel {} actived", ctx.channel());
        ctx.read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel {} inactived", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RedisRequest msg) throws Exception {
        log.debug("Request received: {}", msg);
        var procedure = commandProcedures.get(msg.command().content());
        if (procedure == null) {
            var cmd = msg.command().textValue();
            ctx.writeAndFlush(RespMessages.error("unknown command `" + cmd + "`")).addListener(RespUtil.readNext());
        } else {
            procedure.accept(ctx, msg);
        }
    }

}
