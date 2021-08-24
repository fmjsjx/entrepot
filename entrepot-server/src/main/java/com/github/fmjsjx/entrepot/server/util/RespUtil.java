package com.github.fmjsjx.entrepot.server.util;

import java.util.function.BiConsumer;

import com.github.fmjsjx.libnetty.resp.CachedBulkStringMessage;
import com.github.fmjsjx.libnetty.resp.CachedErrorMessage;
import com.github.fmjsjx.libnetty.resp.DefaultArrayMessage;
import com.github.fmjsjx.libnetty.resp.RedisRequest;
import com.github.fmjsjx.libnetty.resp.RespErrorMessage;
import com.github.fmjsjx.libnetty.resp.RespMessages;
import com.github.fmjsjx.libnetty.resp3.DefaultMapMessage;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class RespUtil {

    public static final RespErrorMessage WRONG_NUMBER_OF_ARGUMENTS_FOR_COMMAND_PING = RespMessages
            .wrongNumberOfArgumentsForCommand("ping");
    private static final RespErrorMessage WRONG_NUMBER_OF_ARGUMENTS_FOR_COMMAND_HELLO = RespMessages
            .wrongNumberOfArgumentsForCommand("hello");
    private static final CachedErrorMessage PROTO_ERROR = CachedErrorMessage
            .createAscii("NOPROTO sorry this protocol version is not supported");

    public static final ChannelFutureListener READ_NEXT = cf -> {
        if (cf.isSuccess()) {
            cf.channel().read();
        }
    };

    public static final ChannelFutureListener readNext() {
        return READ_NEXT;
    }

    public static final BiConsumer<ChannelHandlerContext, RedisRequest> ping() {
        return (ctx, msg) -> {
            int size = msg.size();
            if (msg.size() == 1) {
                ctx.writeAndFlush(RespMessages.pong()).addListener(READ_NEXT);
            } else if (size == 2) {
                ctx.writeAndFlush(msg.argument(1).retainedDuplicate()).addListener(READ_NEXT);
            } else {
                ctx.writeAndFlush(WRONG_NUMBER_OF_ARGUMENTS_FOR_COMMAND_PING).addListener(READ_NEXT);
            }
        };
    }

    public static final AttributeKey<Boolean> KEY_SUPPORT_RESP3 = AttributeKey.valueOf("supportResp3");

    public static final CachedBulkStringMessage SERVER_KEY = CachedBulkStringMessage.createAscii("server");
    public static final CachedBulkStringMessage VERSION_KEY = CachedBulkStringMessage.createAscii("version");
    public static final CachedBulkStringMessage PROTO_KEY = CachedBulkStringMessage.createAscii("proto");

    public static final BiConsumer<ChannelHandlerContext, RedisRequest> hello(String server, String version) {
        var serverValue = CachedBulkStringMessage.createUtf8(server);
        var versionValue = CachedBulkStringMessage.createAscii(version);
        var two = RespMessages.integer(2);
        var three = RespMessages.integer(3);
        return (ctx, msg) -> {
            if (msg.size() < 2) {
                ctx.writeAndFlush(WRONG_NUMBER_OF_ARGUMENTS_FOR_COMMAND_HELLO).addListener(RespUtil.readNext());
            } else {
                try {
                    switch (msg.argument(1).intValue()) {
                    case 2:
                        var array = new DefaultArrayMessage<>(SERVER_KEY, serverValue, VERSION_KEY, versionValue,
                                PROTO_KEY, two);
                        ctx.writeAndFlush(array).addListener(READ_NEXT);
                        break;
                    case 3:
                        var map = new DefaultMapMessage<>();
                        map.put(SERVER_KEY, serverValue);
                        map.put(VERSION_KEY, versionValue);
                        map.put(PROTO_KEY, three);
                        ctx.writeAndFlush(map).addListener(READ_NEXT);
                        break;
                    default:
                        throw new Exception(); // protocol version error
                    }
                } catch (Exception e) {
                    ctx.writeAndFlush(PROTO_ERROR).addListener(READ_NEXT);
                }
            }
        };
    }

    public static final BiConsumer<ChannelHandlerContext, RedisRequest> alwaysOk() {
        return (ctx, msg) -> {
            ctx.writeAndFlush(RespMessages.ok()).addListener(READ_NEXT);
        };
    }

    public static final BiConsumer<ChannelHandlerContext, RedisRequest> quit() {
        return (ctx, msg) -> {
            ctx.writeAndFlush(RespMessages.ok()).addListener(ChannelFutureListener.CLOSE);
        };
    }

    public static final class Resp3Supported {

    }

    private RespUtil() {
    }

}
