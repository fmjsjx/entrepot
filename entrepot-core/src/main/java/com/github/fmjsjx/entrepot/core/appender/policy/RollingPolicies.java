package com.github.fmjsjx.entrepot.core.appender.policy;

import com.github.fmjsjx.entrepot.core.util.PlaceholderUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RollingPolicies {

    public static final TimeBasedPolicy timeBasedPolicy(String namePattern) {
        var result = PlaceholderUtil.convertDateTime(namePattern);
        return new TimeBasedPolicy(result.getPattern(), result.getPeriod());
    }

}
