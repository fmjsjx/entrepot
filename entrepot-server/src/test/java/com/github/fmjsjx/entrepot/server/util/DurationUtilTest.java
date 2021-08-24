package com.github.fmjsjx.entrepot.server.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class DurationUtilTest {

    @Test
    public void testParse() {
        try {
            assertEquals(Duration.ofSeconds(3), DurationUtil.parse("3s"));
            assertEquals(Duration.ofSeconds(3), DurationUtil.parse("3S"));
            assertEquals(Duration.ofSeconds(60), DurationUtil.parse("60s"));
            assertEquals(Duration.ofMillis(200), DurationUtil.parse("200ms"));
            assertEquals(Duration.ofNanos(200_000), DurationUtil.parse("200000ns"));
            assertEquals(Duration.ofMinutes(5), DurationUtil.parse("5m"));
            assertEquals(Duration.ofMinutes(5), DurationUtil.parse("5M"));
            assertEquals(Duration.ofHours(2), DurationUtil.parse("2h"));
            assertEquals(Duration.ofHours(2), DurationUtil.parse("2H"));
            assertEquals(Duration.ofDays(1), DurationUtil.parse("1d"));
            assertEquals(Duration.ofDays(3), DurationUtil.parse("3D"));
            assertEquals(Duration.ofDays(7), DurationUtil.parse("7d"));
        } catch (Exception e) {
            fail(e);
        }
        try {
            DurationUtil.parse("123");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            DurationUtil.parse("123o");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
}
