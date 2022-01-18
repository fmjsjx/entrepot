package com.github.fmjsjx.entrepot.server.conf;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;

import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.entrepot.server.util.DurationUtil;

import io.netty.util.internal.ObjectUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServerProperties {

    private static final Duration defaultConnectionTimeout = Duration.ofSeconds(60);

    private static final DataSize defaultMaxHttpPostSize = DataSize.ofMegabytes(16);

    private static final InetAddress toAddress(String address) {
        if (address == null) {
            return null;
        }
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("unknown address `" + address + "`", e);
        }
    }

    /**
     * The type of the server.
     * <p>
     * The default is {@code http}.
     */
    private ServerType type;

    /**
     * The port to which the server should bind.
     */
    private int port;

    /**
     * Network address to which the server should bind.
     */
    private InetAddress address;

    /**
     * Time that connectors wait for another HTTP request before closing the
     * connection.
     * <p>
     * The default is {@code 60s}.
     */
    private Duration connectionTimeout;

    /**
     * Maximum size of the HTTP post content.
     * <p>
     * The default is {@code 16MB}.
     */
    private DataSize maxHttpPostSize;

    /**
     * SSL properties.
     */
    private SslProperties ssl;

    /**
     * Constructor for YAML parser.
     * 
     * @param type              the type
     * @param port              the port
     * @param address           the address
     * @param connectionTimeout the connection timeout
     * @param maxHttpPostSize   the maximum size of the HTTP post content
     * @param ssl               the SSL
     */
    @JsonCreator
    public ServerProperties(@JsonProperty("type") String type, @JsonProperty(value = "port", required = true) int port,
            @JsonProperty("address") String address, @JsonProperty("connection-timeout") String connectionTimeout,
            @JsonProperty("max-http-post-size") String maxHttpPostSize, @JsonProperty("ssl") SslProperties ssl) {
        this.type = ServerType.of(type);
        this.port = ObjectUtil.checkInRange(port, 1, 65535, "port");
        this.address = toAddress(address);
        this.connectionTimeout = connectionTimeout == null ? defaultConnectionTimeout
                : DurationUtil.parse(connectionTimeout);
        this.maxHttpPostSize = maxHttpPostSize == null ? defaultMaxHttpPostSize : DataSize.parse(maxHttpPostSize);
        this.ssl = ssl == null ? new SslProperties() : ssl;
    }

    public boolean sslEnabled() {
        var ssl = this.ssl;
        return ssl != null && ssl.isEnabled();
    }

    public SocketAddress socketAddress() {
        if (address == null) {
            return new InetSocketAddress(port);
        }
        return new InetSocketAddress(address, port);
    }

    /**
     * Enumeration of server type.
     */
    public enum ServerType {

        HTTP, RESP, RESP3;

        public static final ServerType of(String type) {
            if (type == null) {
                return HTTP;
            }
            switch (type.toLowerCase()) {
            default:
                log.warn("Unknown server type `{}`, use the default server type `http` instead", type);
            case "http":
                return HTTP;
            case "resp":
                return RESP;
            case "resp3":
                return RESP3;
            }
        }

    }

}
