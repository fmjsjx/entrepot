package com.github.fmjsjx.entrepot.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.github.fmjsjx.entrepot.core.Constants;
import com.github.fmjsjx.entrepot.core.appender.policy.TimeBasedPolicy.RollingPeriod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceholderUtil {

    private static final class PlaceholderPatterns {

        private static final Pattern dateTime = Pattern.compile(Constants.DATETIME_PLACEHOLDER_REGEX);
    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    public static final class DateTimeResult {

        private final String pattern;
        private final RollingPeriod period;

    }

    public static final DateTimeResult convertDateTime(String namePattern) {
        var pattern = PlaceholderPatterns.dateTime;
        var matcher = pattern.matcher(namePattern);
        var b = new StringBuilder();
        RollingPeriod period = null;
        List<String> datetimePatterns = new ArrayList<>();
        var lastIndex = 0;
        for (; matcher.find();) {
            if (lastIndex != matcher.start()) {
                b.append("'").append(namePattern.substring(lastIndex, matcher.start())).append("'");
            }
            lastIndex = matcher.end();
            var datetimePattern = matcher.group(2);
            datetimePatterns.add(datetimePattern);
            var p = rollingPeriod(datetimePattern);
            if (period == null || p != null && p.ordinal() > period.ordinal()) {
                period = p;
            }
            b.append(datetimePattern);
        }
        if (period == null) {
            throw new IllegalArgumentException("missing rolling period in " + datetimePatterns);
        }
        b.append("'").append(namePattern.substring(lastIndex)).append("'");
        var namespacePattern = b.toString();
        return new DateTimeResult(namespacePattern, period);
    }

    private static final RollingPeriod rollingPeriod(String datetimePattern) {
        if (datetimePattern.contains("mm")) {
            return RollingPeriod.MINUTELY;
        } else if (datetimePattern.contains("HH")) {
            return RollingPeriod.HOURLY;
        } else if (datetimePattern.contains("dd")) {
            return RollingPeriod.DAILY;
        } else if (datetimePattern.contains("MM")) {
            return RollingPeriod.MONTHLY;
        }
        return null;
    }

}
