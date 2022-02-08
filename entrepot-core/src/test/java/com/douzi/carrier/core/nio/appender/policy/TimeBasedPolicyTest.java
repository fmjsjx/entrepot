package com.douzi.carrier.core.nio.appender.policy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.github.fmjsjx.entrepot.core.appender.RollingPolicy.TriggeringPhase;
import com.github.fmjsjx.entrepot.core.appender.policy.TimeBasedPolicy;
import com.github.fmjsjx.entrepot.core.appender.policy.TimeBasedPolicy.RollingPeriod;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;

public class TimeBasedPolicyTest {

    @Test
    public void testCanTriggered() {
        var policy = new TimeBasedPolicy("'test-'yyyy-MM-dd'.log'", RollingPeriod.DAILY);
        assertEquals(true, policy.canTriggered(TriggeringPhase.BEFORE_APPEND));
        assertEquals(false, policy.canTriggered(TriggeringPhase.AFTER_APPEND));
        try {
            policy.canTriggered(null);
            fail("should throw exception here");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testUpdateDaily() {
        var pattern = "'test-'yyyy-MM-dd'.log'";
        var policy = new TimeBasedPolicy(pattern, RollingPeriod.DAILY);
        var datetime = LocalDate.now().minusDays(1).atStartOfDay();
        var timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        var namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 1000_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 3600_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 86399_000, 0));
        assertEquals(namespace, policy.namespace());
        datetime = datetime.plusDays(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
    }

    @Test
    public void testUpdateHourly() {
        var pattern = "'test-'yyyy-MM-dd'T'HH'.log'";
        var policy = new TimeBasedPolicy(pattern, RollingPeriod.HOURLY);
        var datetime = LocalDate.now().minusDays(1).atStartOfDay();
        var timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        var namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 1000_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 3399_000, 0));
        assertEquals(namespace, policy.namespace());
        datetime = datetime.plusHours(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());

        datetime = datetime.plusDays(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
    }

    @Test
    public void testUpdateMinutely() {
        var pattern = "'test-'yyyy-MM-dd'T'HH_MM'.log'";
        var policy = new TimeBasedPolicy(pattern, RollingPeriod.MINUTELY);
        var datetime = LocalDate.now().minusDays(1).atStartOfDay();
        var timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        var namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 10_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 59_000, 0));
        assertEquals(namespace, policy.namespace());
        datetime = datetime.plusMinutes(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());

        datetime = datetime.plusDays(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
    }

    @Test
    public void testUpdateMonthly() {
        var pattern = "'test-'yyyy-MM'.log'";
        var policy = new TimeBasedPolicy(pattern, RollingPeriod.MONTHLY);
        var datetime = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        var timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        var namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
        
        assertEquals(false, policy.update(timeMillis + 1000_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 3600_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 86400_000, 0));
        assertEquals(namespace, policy.namespace());
        assertEquals(false, policy.update(timeMillis + 864000_000, 0));
        assertEquals(namespace, policy.namespace());
        
        timeMillis = DateTimeUtil.toEpochSecond(datetime.plusMonths(1).minusSeconds(1)) * 1000;
        assertEquals(false, policy.update(timeMillis, 0));
        assertEquals(namespace, policy.namespace());
        
        datetime = datetime.plusMonths(1);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());

        datetime = datetime.plusMonths(3);
        timeMillis = DateTimeUtil.toEpochSecond(datetime) * 1000;
        assertEquals(true, policy.update(timeMillis, 0));
        namespace = datetime.format(DateTimeFormatter.ofPattern(pattern));
        assertEquals(namespace, policy.namespace());
    }
}
