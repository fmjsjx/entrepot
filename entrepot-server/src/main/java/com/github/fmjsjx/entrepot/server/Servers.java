package com.github.fmjsjx.entrepot.server;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.PATCH;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fmjsjx.entrepot.core.wharf.DefaultHangar;
import com.github.fmjsjx.entrepot.core.wharf.DynamicWharves;
import com.github.fmjsjx.entrepot.server.conf.EntrepotServerProperties;
import com.github.fmjsjx.entrepot.server.conf.WharfType;
import com.github.fmjsjx.entrepot.server.conf.HttpRouteProperties;
import com.github.fmjsjx.entrepot.server.conf.RespCommandProperties;
import com.github.fmjsjx.entrepot.server.conf.ServerProperties;
import com.github.fmjsjx.entrepot.server.conf.ServerProperties.ServerType;
import com.github.fmjsjx.entrepot.server.cook.Cook;
import com.github.fmjsjx.entrepot.server.cook.MergeJsonFieldsCook;
import com.github.fmjsjx.entrepot.server.resp.Resp3ServerInitializer;
import com.github.fmjsjx.entrepot.server.resp.RespServerInitializer;
import com.github.fmjsjx.entrepot.server.util.RespUtil;
import com.github.fmjsjx.entrepot.server.util.SystemUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;
import com.github.fmjsjx.libnetty.handler.ssl.SslContextProvider;
import com.github.fmjsjx.libnetty.handler.ssl.SslContextProviders;
import com.github.fmjsjx.libnetty.http.server.DefaultHttpServer;
import com.github.fmjsjx.libnetty.http.server.HttpServer;
import com.github.fmjsjx.libnetty.http.server.middleware.AccessLogger;
import com.github.fmjsjx.libnetty.http.server.middleware.AccessLogger.LogFormat;
import com.github.fmjsjx.libnetty.http.server.middleware.AccessLogger.Slf4jLoggerWrapper;
import com.github.fmjsjx.libnetty.http.server.middleware.Router;
import com.github.fmjsjx.libnetty.resp.RedisRequest;
import com.github.fmjsjx.libnetty.resp.RespMessages;
import com.github.fmjsjx.libnetty.transport.TransportLibrary;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.util.CharsetUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Servers implements InitializingBean, DisposableBean {

    private static final CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
            .allowedRequestMethods(GET, POST, PUT, PATCH, DELETE).allowedRequestHeaders("*").allowNullOrigin().build();

    private static final Predicate<HttpHeaders> toContentTypeValidator(List<String> allowedContentTypes) {
        if (allowedContentTypes.isEmpty()) {
            return headers -> true;
        }
        if (allowedContentTypes.size() == 1) {
            var type = allowedContentTypes.get(0);
            return headers -> {
                var contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
                var mimeType = HttpUtil.getMimeType(contentType);
                return type.equals(mimeType);
            };
        }
        var types = Set.copyOf(allowedContentTypes);
        return headers -> {
            var contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
            var mimeType = HttpUtil.getMimeType(contentType);
            if (mimeType == null) {
                return false;
            }
            return types.contains(mimeType);
        };
    }

    private static final Cook generateCook(HttpRouteProperties route) {
        switch (route.getProcessor()) {
        case "merge-json-fields":
            var mergeJsonFieldsCook = new MergeJsonFieldsCook(route.getFields());
            return mergeJsonFieldsCook;
        default:
            throw new IllegalArgumentException("Unsupported cook processor `" + route.getProcessor() + "`");
        }
    }

    private static final Cook generateCook(RespCommandProperties command) {
        switch (command.getProcessor()) {
        case "merge-json-fields":
            var mergeJsonFieldsCook = new MergeJsonFieldsCook(command.getFields());
            return mergeJsonFieldsCook;
        default:
            throw new IllegalArgumentException("Unsupported cook processor `" + command.getProcessor() + "`");
        }
    }

    @Autowired
    private AppProperties appProperties;

    private volatile Timer forceTimer;
    private volatile List<HttpServer> httpServers;
    private volatile List<SocketAddress> respServerAddresses;
    private volatile List<SocketAddress> resp3ServerAddresses;
    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;

    @Override
    public synchronized void afterPropertiesSet() throws Exception {
        var file = SystemUtil.confFile("entrepot-server.yml");
        log.info("Loading configuration {}", file);
        var conf = EntrepotServerProperties.loadFromYaml(file);
        log.debug("Loaded conf: {}", conf);

        // validate servers
        var confServers = conf.getServers();
        if (confServers.isEmpty()) {
            throw new IllegalArgumentException("no server present on entrepot-server.yml");
        }
        // validate HTTP routes
        var confHttpRoutes = conf.getHttpRoutes();
        if (confHttpRoutes.isEmpty()
                && confServers.stream().filter(cfg -> cfg.getType() == ServerType.HTTP).count() > 0) {
            throw new IllegalArgumentException("no HTTP route present on entrepot-server.yml");
        }
        confHttpRoutes.forEach(HttpRouteProperties::validate);
        // validate RESP commands
        var confRespCommands = conf.getRespCommands();
        if (confRespCommands.isEmpty() && confServers.stream()
                .filter(cfg -> cfg.getType() == ServerType.RESP || cfg.getType() == ServerType.RESP3).count() > 0) {
            throw new IllegalArgumentException("no RESP/RESP3 commands present on entrepot-server.yml");
        }
        // validate storage
        var confStorage = conf.getStorage();
        confStorage.validate();
        var forcePeriod = confStorage.getForcePeriod();
        if (forcePeriod != null) {
            forceTimer = new HashedWheelTimer(new DefaultThreadFactory("force-timer"));
        }
        var forceTimer = this.forceTimer;
        var appendLineFeed = confStorage.getAppendLineFeed().code();
        var wharves = new DynamicWharves(name -> new DefaultHangar(name, confStorage.toRollingFileAppender(name), null,
                appendLineFeed, forcePeriod, forceTimer));
        // HTTP router
        var router = new Router();
        for (var route : confHttpRoutes) {
            addRoute(wharves, router, route);
        }
        log.debug("Configure HTTP router: {}", router);

        // RESP/RESP3 commands
        var commands = new LinkedHashMap<String, BiConsumer<ChannelHandlerContext, RedisRequest>>();
        for (var command : confRespCommands) {
            addCommand(wharves, commands, command);
        }
        // start up servers
        var httpServers = this.httpServers = new ArrayList<>(
                (int) confServers.stream().filter(cfg -> cfg.getType() == ServerType.HTTP).count());
        var respServerAddresses = this.respServerAddresses = new ArrayList<>(
                (int) confServers.stream().filter(cfg -> cfg.getType() == ServerType.RESP).count());
        var resp3ServerAddresses = this.resp3ServerAddresses = new ArrayList<>(
                (int) confServers.stream().filter(cfg -> cfg.getType() == ServerType.RESP3).count());
        var transportLibrary = TransportLibrary.getDefault();
        var bossGroup = this.bossGroup = transportLibrary.createGroup(confServers.size(),
                new DefaultThreadFactory("boss"));
        var workerGroup = this.workerGroup = transportLibrary.createGroup(conf.getIoThreads(),
                new DefaultThreadFactory("worker"));
        var channelClass = transportLibrary.serverChannelClass();
        for (var serverCfg : confServers) {
            var sslContextProvider = sslContextProvider(serverCfg);
            if (serverCfg.getType() == ServerType.HTTP) {
                var server = new DefaultHttpServer(serverCfg.getPort()).address(serverCfg.getAddress())
                        .corsConfig(corsConfig).soBackLog(1024).timeout(serverCfg.getConnectionTimeout())
                        .maxContentLength((int) serverCfg.getMaxHttpPostSize().toBytes())
                        .transport(bossGroup, workerGroup, channelClass);
                sslContextProvider.ifPresent(server::enableSsl);
                server.defaultHandlerProvider()
                        .addLast(new AccessLogger(new Slf4jLoggerWrapper("accessLogger"), LogFormat.BASIC2))
                        .addLast(router);
                httpServers.add(server);
                server.startup();
                log.info("Server {} started.", server);
            } else if (serverCfg.getType() == ServerType.RESP) {
                var bootstrap = new ServerBootstrap().group(bossGroup, workerGroup).channel(channelClass)
                        .option(ChannelOption.SO_BACKLOG, 512).childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.AUTO_READ, false).childHandler(new RespServerInitializer(
                                (int) serverCfg.getConnectionTimeout().getSeconds(), sslContextProvider, commands));
                var addr = serverCfg.socketAddress();
                bootstrap.bind(addr).sync();
                respServerAddresses.add(addr);
                log.info("Server RESP at {} started.", addr);
            } else if (serverCfg.getType() == ServerType.RESP3) {
                var bootstrap = new ServerBootstrap().group(bossGroup, workerGroup).channel(channelClass)
                        .option(ChannelOption.SO_BACKLOG, 512).childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.AUTO_READ, false)
                        .childHandler(new Resp3ServerInitializer(appProperties.getVersion(),
                                (int) serverCfg.getConnectionTimeout().getSeconds(), sslContextProvider, commands));
                var addr = serverCfg.socketAddress();
                bootstrap.bind(addr).sync();
                resp3ServerAddresses.add(addr);
                log.info("Server RESP3 at {} started.", addr);
            }
        }
    }

    private Optional<SslContextProvider> sslContextProvider(ServerProperties serverCfg) {
        Optional<SslContextProvider> sslContextProvider = Optional.empty();
        if (serverCfg.sslEnabled()) {
            var ssl = serverCfg.getSsl();
            var keyCertChainFile = new File(ssl.getKeyCertChainFile());
            var keyFile = new File(ssl.getKeyFile());
            sslContextProvider = Optional
                    .of(SslContextProviders.forServer(keyCertChainFile, keyFile, ssl.getKeyPassword()));
        }
        return sslContextProvider;
    }

    private void addRoute(DynamicWharves wharves, Router router, HttpRouteProperties route) {
        var pathVar = route.getPathVar();
        var queryVar = route.getQueryVar();
        var method = HttpMethod.valueOf(route.getMethod());
        var path = route.getPath();
        if (route.getType() == WharfType.RAW) {
            // raw data
            if (HttpMethod.GET.equals(method)) {
                // GET method
                if (StringUtil.isBlank(queryVar)) {
                    throw new IllegalArgumentException("missing required parameter `query-var`");
                }
                router.add(path, method, ctx -> {
                    try {
                        var name = ctx.pathVariables().getString(pathVar).get();
                        var hangar = wharves.get(name)
                                .orElseThrow(() -> new IllegalArgumentException("unknown hangar `" + name + "`"));
                        var data = ctx.queryParameter(queryVar)
                                .orElseThrow(() -> new IllegalArgumentException(queryVar)).get(0)
                                .getBytes(CharsetUtil.UTF_8);
                        var buf = ctx.alloc().buffer(data.length);
                        buf.writeBytes(data);
                        return hangar.transferFrom(buf).handleAsync((nil, e) -> {
                            if (e != null) {
                                return ctx.respondError(e);
                            }
                            return ctx.simpleRespond(HttpResponseStatus.OK);
                        }, ctx.eventLoop()).thenCompose(Function.identity());
                    } catch (Exception e) {
                        return ctx.respondError(e);
                    }
                });
            } else {
                // check content types
                var contentTypeValidator = toContentTypeValidator(route.getAllowedContentTypes());
                router.add(path, method, ctx -> {
                    try {
                        if (!contentTypeValidator.test(ctx.headers())) {
                            return ctx.simpleRespond(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
                        }
                        var name = ctx.pathVariables().getString(pathVar).get();
                        var hangar = wharves.get(name).get();
                        return hangar.transferFrom(ctx.body().retain()).handleAsync((nil, e) -> {
                            if (e != null) {
                                return ctx.respondError(e);
                            }
                            return ctx.simpleRespond(HttpResponseStatus.OK);
                        }, ctx.eventLoop()).thenCompose(Function.identity());
                    } catch (Exception e) {
                        return ctx.respondError(e);
                    }
                });
            }
        } else if (route.getType() == WharfType.COOK) {
            var cook = generateCook(route);
            if (HttpMethod.GET.equals(method)) {
                // GET method
                if (StringUtil.isBlank(queryVar)) {
                    throw new IllegalArgumentException("missing required parameter `query-var`");
                }
                router.add(path, method, ctx -> {
                    try {
                        var name = ctx.pathVariables().getString(pathVar).get();
                        var hangar = wharves.get(name)
                                .orElseThrow(() -> new IllegalArgumentException("unknown hangar `" + name + "`"));
                        var raw = ctx.queryParameter(queryVar).orElseThrow(() -> new IllegalArgumentException(queryVar))
                                .get(0).getBytes(CharsetUtil.UTF_8);
                        var data = cook.cook(raw, ctx.remoteAddress());
                        var buf = ctx.alloc().buffer(data.length);
                        buf.writeBytes(data);
                        return hangar.transferFrom(buf).handleAsync((nil, e) -> {
                            if (e != null) {
                                return ctx.respondError(e);
                            }
                            return ctx.simpleRespond(HttpResponseStatus.OK);
                        }, ctx.eventLoop()).thenCompose(Function.identity());
                    } catch (Exception e) {
                        return ctx.respondError(e);
                    }
                });
            } else {
                // check content types
                var contentTypeValidator = toContentTypeValidator(route.getAllowedContentTypes());
                router.add(path, method, ctx -> {
                    try {
                        if (!contentTypeValidator.test(ctx.headers())) {
                            return ctx.simpleRespond(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
                        }
                        var name = ctx.pathVariables().getString(pathVar).get();
                        var hangar = wharves.get(name).get();
                        var raw = ByteBufUtil.getBytes(ctx.body());
                        var data = cook.cook(raw, ctx.remoteAddress());
                        var buf = ctx.alloc().buffer(data.length);
                        buf.writeBytes(data);
                        return hangar.transferFrom(buf).handleAsync((nil, e) -> {
                            if (e != null) {
                                return ctx.respondError(e);
                            }
                            return ctx.simpleRespond(HttpResponseStatus.OK);
                        }, ctx.eventLoop()).thenCompose(Function.identity());
                    } catch (Exception e) {
                        return ctx.respondError(e);
                    }
                });
            }
        }
    }

    private void addCommand(DynamicWharves wharves,
            Map<String, BiConsumer<ChannelHandlerContext, RedisRequest>> commands, RespCommandProperties command) {
        var cmd = command.getCommand();
        if (command.isBatchEnabled() && cmd.batchSupported()) {
            // batch
            if (command.getType() == WharfType.RAW) {
                commands.put(cmd.name(), batchRaw(wharves, command));
            } else if (command.getType() == WharfType.COOK) {
                commands.put(cmd.name(), batchCook(wharves, command));
            }
        } else {
            if (command.getType() == WharfType.RAW) {
                commands.put(cmd.name(), raw(wharves, command));
            } else if (command.getType() == WharfType.COOK) {
                commands.put(cmd.name(), cook(wharves, command));
            }
        }
    }

    private BiConsumer<ChannelHandlerContext, RedisRequest> batchRaw(DynamicWharves wharves,
            RespCommandProperties command) {
        var resultMapper = command.getCommand().resultMapper();
        return (ctx, msg) -> {
            try {
                var name = msg.argument(1).textValue();
                var hangar = wharves.get(name).get();
                var ch = ctx.channel();
                if (msg.size() == 3) {
                    var raw = msg.argument(2).content().retain();
                    hangar.transferFrom(raw).handleAsync((nil, e) -> {
                        if (e != null) {
                            return RespMessages.error(e.toString());
                        }
                        return resultMapper.apply(1);
                    }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
                } else {
                    int dataSize = msg.size() - 2;
                    var datas = new ByteBuf[dataSize];
                    for (int i = 0; i < dataSize; i++) {
                        datas[i] = msg.argument(i + 2).content().retain();
                    }
                    hangar.transferFrom(datas).handleAsync((nil, e) -> {
                        if (e != null) {
                            return RespMessages.error(e.toString());
                        }
                        return resultMapper.apply(dataSize);
                    }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
                }
            } catch (Exception e) {
                ctx.writeAndFlush(RespMessages.error(e.toString())).addListener(RespUtil.readNext());
            }
        };
    }

    private BiConsumer<ChannelHandlerContext, RedisRequest> batchCook(DynamicWharves wharves,
            RespCommandProperties command) {
        var resultMapper = command.getCommand().resultMapper();
        var cook = generateCook(command);
        return (ctx, msg) -> {
            try {
                var name = msg.argument(1).textValue();
                var hangar = wharves.get(name).get();
                var ch = ctx.channel();
                if (msg.size() == 3) {
                    var raw = ByteBufUtil.getBytes(msg.argument(2).content());
                    var data = cook.cook(raw, ((InetSocketAddress) ch.remoteAddress()).getHostString());
                    var buf = ctx.alloc().buffer(data.length);
                    buf.writeBytes(data);
                    hangar.transferFrom(buf).handleAsync((nil, e) -> {
                        if (e != null) {
                            return RespMessages.error(e.toString());
                        }
                        return resultMapper.apply(1);
                    }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
                } else {
                    int dataSize = msg.size() - 2;
                    var datas = new ByteBuf[dataSize];
                    for (int i = 0; i < dataSize; i++) {
                        var raw = ByteBufUtil.getBytes(msg.argument(i + 2).content());
                        var data = cook.cook(raw, ((InetSocketAddress) ch.remoteAddress()).getHostString());
                        var buf = ctx.alloc().buffer(data.length);
                        buf.writeBytes(data);
                        datas[i] = buf;
                    }
                    hangar.transferFrom(datas).handleAsync((nil, e) -> {
                        if (e != null) {
                            return RespMessages.error(e.toString());
                        }
                        return resultMapper.apply(dataSize);
                    }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
                }
            } catch (Exception e) {
                ctx.writeAndFlush(RespMessages.error(e.toString())).addListener(RespUtil.readNext());
            }
        };
    }

    private BiConsumer<ChannelHandlerContext, RedisRequest> raw(DynamicWharves wharves, RespCommandProperties command) {
        var resultMapper = command.getCommand().resultMapper();
        return (ctx, msg) -> {
            try {
                var name = msg.argument(1).textValue();
                var hangar = wharves.get(name).get();
                var ch = ctx.channel();
                var raw = msg.argument(2).content().retain();
                hangar.transferFrom(raw).handleAsync((nil, e) -> {
                    if (e != null) {
                        return RespMessages.error(e.toString());
                    }
                    return resultMapper.apply(1);
                }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
            } catch (Exception e) {
                ctx.writeAndFlush(RespMessages.error(e.toString())).addListener(RespUtil.readNext());
            }
        };
    }

    private BiConsumer<ChannelHandlerContext, RedisRequest> cook(DynamicWharves wharves,
            RespCommandProperties command) {
        var resultMapper = command.getCommand().resultMapper();
        var cook = generateCook(command);
        return (ctx, msg) -> {
            try {
                var name = msg.argument(1).textValue();
                var hangar = wharves.get(name).get();
                var ch = ctx.channel();
                var raw = ByteBufUtil.getBytes(msg.argument(2).content());
                var data = cook.cook(raw, ((InetSocketAddress) ch.remoteAddress()).getHostString());
                var buf = ctx.alloc().buffer(data.length);
                buf.writeBytes(data);
                hangar.transferFrom(buf).handleAsync((nil, e) -> {
                    if (e != null) {
                        return RespMessages.error(e.toString());
                    }
                    return resultMapper.apply(1);
                }, ch.eventLoop()).thenAccept(resp -> ch.writeAndFlush(resp).addListener(RespUtil.readNext()));
            } catch (Exception e) {
                ctx.writeAndFlush(RespMessages.error(e.toString())).addListener(RespUtil.readNext());
            }
        };
    }

    @Override
    public synchronized void destroy() throws Exception {
        var forceTimer = this.forceTimer;
        if (forceTimer != null) {
            forceTimer.stop();
        }
        for (var server : httpServers) {
            if (server != null && server.isRunning()) {
                server.shutdown();
                log.info("Server {} stopped.", server);
            }
        }
        var bossBroup = this.bossGroup;
        if (bossBroup != null && !bossBroup.isShuttingDown()) {
            bossBroup.shutdownGracefully();
        }
        var workerGroup = this.workerGroup;
        if (workerGroup != null && !workerGroup.isShuttingDown()) {
            workerGroup.shutdownGracefully();
        }
        for (var address : respServerAddresses) {
            log.info("Server RESP at {} stopped.", address);
        }
        for (var address : resp3ServerAddresses) {
            log.info("Server RESP3 at {} stopped.", address);
        }
    }

}
