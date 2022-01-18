package com.github.fmjsjx.entrepot.server.conf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.entrepot.server.util.SystemUtil;
import com.github.fmjsjx.libcommon.util.StringUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * SNI properties class.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SniProperties {

    public static final SniProperties disable() {
        return new SniProperties();
    }

    /**
     * Whether to enable SNI support or not.
     * <p>
     * The default is {@code false}.
     */
    private boolean enabled;

    /**
     * Path string of the mapping file stores the mapping of domain name to
     * {@link KeyCertProperties}.
     */
    private String mappingFile;

    @JsonCreator
    public SniProperties(@JsonProperty("enabled") boolean enabled, @JsonProperty("mapping-file") String mappingFile) {
        this.enabled = enabled;
        this.mappingFile = mappingFile;
        validate();
    }

    private void validate() {
        if (enabled) {
            if (StringUtil.isEmpty(mappingFile)) {
                throw new IllegalArgumentException("missing required parameter `mapping-file`");
            }
            if (!mappingFile.startsWith("/")) {
                mappingFile = SystemUtil.confDir() + "/" + mappingFile;
            }
            SystemUtil.validateFile(mappingFile);
        }
    }

}
