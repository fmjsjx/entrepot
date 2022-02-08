package com.github.fmjsjx.entrepot.server.conf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fmjsjx.entrepot.core.wharf.DefaultHangar;
import com.github.fmjsjx.entrepot.server.util.DurationUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Properties class for storage.
 */
@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
public class StorageProperties {

    private static final String defaultDir = "storage";
    private static final Duration minForcePeriod = Duration.ofMillis(100);

    /**
     * The path of the storage base directory.
     * <p>
     * The default value is {@code storage}.
     */
    private String dir;

    /**
     * If true then the program will create sub-directories with the same name of
     * the hangar automatically.
     * <p>
     * The default is {@code false}.
     */
    private boolean nameAsFolder;

    /**
     * The name pattern of the stored file.
     */
    private String filePattern;

    /**
     * The period between each data forces.
     * <p>
     * The minimum value is {@code 100ms}.
     */
    private Duration forcePeriod;
    /**
     * The append line feed mode.
     * <p>
     * The default value is {@code auto}.
     */
    private AppendLineFeed appendLineFeed;

    @JsonCreator
    public StorageProperties(@JsonProperty("dir") String dir, @JsonProperty("name-as-folder") boolean nameAsFolder,
            @JsonProperty("file-pattern") String filePattern, @JsonProperty("force-period") String forcePeriod,
            @JsonProperty("append-line-feed") String appendLineFeed) {
        this.dir = dir == null ? defaultDir : dir;
        this.nameAsFolder = nameAsFolder;
        this.filePattern = filePattern;
        var fp = forcePeriod == null ? null : DurationUtil.parse(forcePeriod);
        if (fp != null && fp.compareTo(minForcePeriod) < 0) {
            fp = minForcePeriod;
        }
        this.forcePeriod = fp;
        this.appendLineFeed = AppendLineFeed.of(appendLineFeed);
    }

    public String fileNamePattern(String name) {
        return filePattern.replace("%name", name);
    }

    public Path toParentPath(String name) {
        return nameAsFolder ? Paths.get(dir, name) : Paths.get(dir);
    }

    public void validate() {
        var file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (!file.isDirectory()) {
                throw new IllegalArgumentException("path `" + dir + "` must be a directory");
            }
        }
    }

    /**
     * Enumeration of appending line feed mode.
     */
    public enum AppendLineFeed {

        NEVER(DefaultHangar.NEVER), AUTO(DefaultHangar.AUTO), ALWAYS(DefaultHangar.ALWAYS);

        private final int code;

        private AppendLineFeed(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public static final AppendLineFeed of(String mode) {
            if (mode == null) {
                return AUTO;
            }
            switch (mode.toLowerCase()) {
            case "never":
                return NEVER;
            case "always":
                return ALWAYS;
            default:
                log.warn("Unknown append line feed mode `{}`, use the default mode `auto` instead", mode);
            case "auto":
                return AUTO;
            }
        }

    }

}
