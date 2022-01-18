package com.github.fmjsjx.entrepot.server.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.github.fmjsjx.libcommon.util.RuntimeUtil;
import com.github.fmjsjx.libcommon.util.SystemPropertyUtil;

public class SystemUtil {

    private static final class AvailableProcessorsHolder {
        private static final int AVAILABLE_PROCESSORS;

        static {
            var availableProcessors = SystemPropertyUtil.getInt("runtime.availableProcessors", -1);
            if (availableProcessors <= 0) {
                availableProcessors = RuntimeUtil.availableProcessors();
            }
            AVAILABLE_PROCESSORS = availableProcessors;
        }
    }

    public static final int availableProcessors() {
        return AvailableProcessorsHolder.AVAILABLE_PROCESSORS;
    }

    private static final class ConfDirHolder {
        private static final String CONF_DIR = System.getProperty("conf.dir", "conf");
    }

    public static final String confDir() {
        return ConfDirHolder.CONF_DIR;
    }

    public static final File confFile(String filename) {
        return new File(confDir(), filename);
    }

    public static final InputStream openConfFile(String filename) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(confFile(filename)));
    }

    public static final void validateFile(String path) {
        var file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("file on path `" + path + "` doesn't exist");
        } else {
            if (!file.isFile()) {
                throw new IllegalArgumentException("path at `" + path + "` must be a file");
            }
        }
    }

    private SystemUtil() {
    }
}
