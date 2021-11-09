package com.github.fmjsjx.entrepot.server.util;

import java.time.Duration;
import java.util.regex.Pattern;

public class DurationUtil {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]+)$");

    public static final Duration parse(String text) {
        var m = PATTERN.matcher(text);
        if (m.matches()) {
            var value = Long.parseLong(m.group(1));
            var unit = m.group(2).toLowerCase();
            return switch (unit) {
            case "ns" -> Duration.ofNanos(value);
            case "ms" -> Duration.ofMillis(value);
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            default -> throw new IllegalArgumentException("unknown duration unit for `" + text + "`");
            };
        }
        throw new IllegalArgumentException("parse duration failed for `" + text + "`");
    }

    private DurationUtil() {
    }

}
