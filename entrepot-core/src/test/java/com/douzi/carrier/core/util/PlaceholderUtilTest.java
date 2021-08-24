package com.douzi.carrier.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.github.fmjsjx.entrepot.core.appender.policy.TimeBasedPolicy.RollingPeriod;
import com.github.fmjsjx.entrepot.core.util.PlaceholderUtil;

public class PlaceholderUtilTest {

    @Test
    public void testConvertDateTime() {
        var result = PlaceholderUtil.convertDateTime("test-%d{yyyy-MM-dd}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'.log'", result.getPattern());
        assertEquals(RollingPeriod.DAILY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%datetime{yyyy-MM-dd}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'.log'", result.getPattern());
        assertEquals(RollingPeriod.DAILY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%d{yyyy-MM}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM'.log'", result.getPattern());
        assertEquals(RollingPeriod.MONTHLY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%datetime{yyyy-MM}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM'.log'", result.getPattern());
        assertEquals(RollingPeriod.MONTHLY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%d{yyyy-MM-dd'T'HH}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'T'HH'.log'", result.getPattern());
        assertEquals(RollingPeriod.HOURLY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%datetime{yyyy-MM-dd'T'HH}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'T'HH'.log'", result.getPattern());
        assertEquals(RollingPeriod.HOURLY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%d{yyyy-MM-dd'T'HH_mm}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'T'HH_mm'.log'", result.getPattern());
        assertEquals(RollingPeriod.MINUTELY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("test-%datetime{yyyy-MM-dd'T'HH_mm}.log");
        assertNotNull(result);
        assertEquals("'test-'yyyy-MM-dd'T'HH_mm'.log'", result.getPattern());
        assertEquals(RollingPeriod.MINUTELY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("%d{yyyy-MM}/test-%d{yyyy-MM-dd}.log");
        assertNotNull(result);
        assertEquals("yyyy-MM'/test-'yyyy-MM-dd'.log'", result.getPattern());
        assertEquals(RollingPeriod.DAILY, result.getPeriod());

        result = PlaceholderUtil.convertDateTime("%datetime{yyyy-MM}/test-%datetime{yyyy-MM-dd}.log");
        assertNotNull(result);
        assertEquals("yyyy-MM'/test-'yyyy-MM-dd'.log'", result.getPattern());
        assertEquals(RollingPeriod.DAILY, result.getPeriod());

        var now = LocalDateTime.now();
        var rolling = now.format(DateTimeFormatter.ofPattern(result.getPattern()));
        var expected = String.format("%d-%02d/test-%d-%02d-%02d.log", now.getYear(), now.getMonthValue(), now.getYear(),
                now.getMonthValue(), now.getDayOfMonth());
        assertEquals(expected, rolling);
    }

}
