package com.github.fmjsjx.entrepot.server.conf;

import static com.github.fmjsjx.entrepot.server.util.SystemUtil.validateFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.libcommon.util.StringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Key and certificate properties class.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class KeyCertProperties {

    /**
     * Path string of the key certificate chain file. The file must be an X.509
     * certificate chain file in PEM format.
     */
    protected String keyCertChainFile;
    /**
     * Path string of the key file. The file must be a PKCS#8 private key file in
     * PEM format.
     */
    protected String keyFile;
    /**
     * The password of the {@code keyFile}, or {@code null} if it's not
     * password-protected.
     */
    protected String keyPassword;

    @JsonCreator
    public KeyCertProperties(@JsonProperty("key-cert-chain-file") String keyCertChainFile,
            @JsonProperty("key-file") String keyFile, @JsonProperty("key-password") String keyPassword) {
        this.keyCertChainFile = keyCertChainFile;
        this.keyFile = keyFile;
        this.keyPassword = StringUtil.isEmpty(keyPassword) ? null : keyPassword;
    }

    public void validate() {
        if (keyCertChainFile == null) {
            throw new IllegalArgumentException("missing required parameter `key-cert-chain-file`");
        }
        validateFile(keyCertChainFile);
        if (keyFile == null) {
            throw new IllegalArgumentException("missing required parameter `key-file`");
        }
        validateFile(keyFile);
    }

}