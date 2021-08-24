package com.github.fmjsjx.entrepot.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final byte LF = '\n';

    public static final String DATETIME_PLACEHOLDER_REGEX = "%d(atetime)?\\{([^%\\{\\}]+)\\}";

}
