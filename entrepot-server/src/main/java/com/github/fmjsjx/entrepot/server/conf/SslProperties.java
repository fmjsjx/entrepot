package com.github.fmjsjx.entrepot.server.conf;

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
public class SslProperties extends KeyCertProperties {

    /**
     * Whether to enable SSL support. The default is false.
     */
    private boolean enabled = false;

    /**
     * SNI properties.
     */
    private SniProperties sni;

    /**
     * Constructor for YAML parser.
     * 
     * @param enabled          {@code true} if enabled
     * @param keyCertChainFile the path string of the key certificate chain file
     * @param keyFile          the path string of the key file
     * @param keyPassword      the password, if present, of the key file
     * @param sni
     */
    @JsonCreator
    public SslProperties(@JsonProperty("enabled") boolean enabled,
            @JsonProperty("key-cert-chain-file") String keyCertChainFile, @JsonProperty("key-file") String keyFile,
            @JsonProperty("key-password") String keyPassword, @JsonProperty("sni") SniProperties sni) {
        super();
        this.enabled = enabled;
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        if (StringUtil.isNotEmpty(keyPassword)) {
            this.keyPassword = keyPassword;
        }
        if (enabled) {
            super.validate();
            if (sni == null) {
                this.sni = SniProperties.disable();
            } else {
                this.sni = sni;
            }
        }
    }

}
