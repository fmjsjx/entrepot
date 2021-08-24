package com.github.fmjsjx.entrepot.server.conf;

import lombok.extern.slf4j.Slf4j;

/**
 * Enumeration of hangar type.
 */
@Slf4j
public enum WharfType {

    RAW, COOK;

    public static final WharfType of(String type) {
        if (type == null) {
            return RAW;
        }
        switch (type) {
        case "cook":
            return COOK;
        default:
            log.warn("Unknown wharf type `{}`, use the default wharf type `raw` instead", type);
        case "raw":
            return RAW;
        }
    }

}
