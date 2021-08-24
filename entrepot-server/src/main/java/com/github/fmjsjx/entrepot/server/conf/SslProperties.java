package com.github.fmjsjx.entrepot.server.conf;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libcommon.util.StringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * SSL properties class.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SslProperties {

    private static final void validateFile(String path) {
        var file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("file on path `" + path + "` doesn't exist");
        } else {
            if (!file.isFile()) {
                throw new IllegalArgumentException("path at `" + path + "` must be a file");
            }
        }
    }

    /**
     * Whether to enable SSL support. The default is false.
     */
    private boolean enabled = false;

    /**
     * Path string of the key certificate chain file. The file must be an X.509
     * certificate chain file in PEM format.
     */
    private String keyCertChainFile;

    /**
     * Path string of the key file. The file must be a PKCS#8 private key file in
     * PEM format.
     */
    private String keyFile;

    /**
     * The password of the {@code keyFile}, or {@code null} if it's not
     * password-protected.
     */
    private String keyPassword;

    /**
     * Constructor for YAML parser.
     * 
     * @param enabled          {@code true} if enabled
     * @param keyCertChainFile the path string of the key certificate chain file
     * @param keyFile          the path string of the key file
     * @param keyPassword      the password, if present, of the key file
     */
    @JsonCreator
    public SslProperties(@JsonProperty("enabled") boolean enabled,
            @JsonProperty("key-cert-chain-file") String keyCertChainFile, @JsonProperty("key-file") String keyFile,
            @JsonProperty("key-password") String keyPassword) {
        this.enabled = enabled;
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        if (enabled) {
            if (keyCertChainFile == null) {
                throw new IllegalArgumentException("missing required parameter `key-cert-chain-file`");
            } else {
                validateFile(keyCertChainFile);
            }
            if (keyFile == null) {
                throw new IllegalArgumentException("missing required parameter `key-file`");
            } else {
                validateFile(keyFile);
            }
            if (StringUtil.isNotEmpty(keyPassword)) {
                this.keyPassword = keyPassword;
            }
        }
    }

}
