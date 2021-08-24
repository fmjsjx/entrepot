package com.github.fmjsjx.entrepot.server.conf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libcommon.yaml.Jackson2YamlLibrary;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Properties class for carrier server.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EntrepotServerProperties {

    public static final EntrepotServerProperties loadFromYaml(File file) throws FileNotFoundException, IOException {
        try (var in = new BufferedInputStream(new FileInputStream(file))) {
            return loadFromYaml(in);
        }
    }

    public static final EntrepotServerProperties loadFromYaml(InputStream in) {
        return Jackson2YamlLibrary.getInstance().loads(in, EntrepotServerProperties.class);
    }

    /**
     * Number of I/O threads to create for the server. When the value is 0, the
     * default, the number is derived from the number of available processors x 2.
     */
    private int ioThreads;

    /**
     * The servers.
     */
    private List<ServerProperties> servers = List.of();

    /**
     * The HTTP routes.
     */
    private List<HttpRouteProperties> httpRoutes = List.of();

    /**
     * The RESP/RESP3 commands.
     */
    private List<RespCommandProperties> respCommands = List.of();

    /**
     * The storage.
     */
    private StorageProperties storage;

    @JsonCreator
    public EntrepotServerProperties(@JsonProperty("io-threads") int ioThreads,
            @JsonProperty("servers") List<ServerProperties> servers,
            @JsonProperty("http.routes") List<HttpRouteProperties> httpRoutes,
            @JsonProperty("resp.commands") List<RespCommandProperties> respCommands,
            @JsonProperty(value = "storage", required = true) StorageProperties storage) {
        this.ioThreads = ioThreads;
        if (servers != null) {
            this.servers = servers;
        }
        if (httpRoutes != null) {
            this.httpRoutes = httpRoutes;
        }
        if (respCommands != null) {
            this.respCommands = respCommands;
        }
        this.storage = storage;
    }

}
