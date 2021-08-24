package com.github.fmjsjx.entrepot.core.appender.policy;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.github.fmjsjx.libcommon.util.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeBasedPolicy extends AbstractRollingPolicy {

    private final String namespacePattern;
    private final RollingPeriod rollingPeriod;
    private final DateTimeFormatter namespaceFotmatter;
    private long nextTimeMillis;
    private String namespace;

    public TimeBasedPolicy(String namespacePattern, RollingPeriod rollingPeriod) {
        this.namespacePattern = requireNonNull(namespacePattern, "namespacePattern must not be null");
        this.rollingPeriod = requireNonNull(rollingPeriod, "rollingPeriod must not be null");
        namespaceFotmatter = DateTimeFormatter.ofPattern(namespacePattern);
    }

    public String getNamespacePattern() {
        return namespacePattern;
    }

    public RollingPeriod getRollingPeriod() {
        return rollingPeriod;
    }

    @Override
    public boolean update(long timeMillis, long size) {
        if (timeMillis >= nextTimeMillis) {
            var datetime = DateTimeUtil.local(timeMillis / 1000);
            log.debug("[{}] datetime before update: {}", this, datetime);
            var date = datetime.toLocalDate();
            // calculate next time
            var nextDatetime = nextDatetime(datetime, date);
            log.debug("[{}] next datetime after update: {}", this, nextDatetime);
            nextTimeMillis = DateTimeUtil.toEpochSecond(nextDatetime) * 1000;
            namespace = datetime.format(namespaceFotmatter);
            log.debug("[{}] after update", this);
            return true;
        }
        return false;
    }

    private LocalDateTime nextDatetime(LocalDateTime datetime, LocalDate date) {
        LocalDateTime nextDatetime;
        if (rollingPeriod == RollingPeriod.DAILY) {
            // daily
            nextDatetime = date.plusDays(1).atStartOfDay();
        } else if (rollingPeriod == RollingPeriod.HOURLY) {
            // hourly
            nextDatetime = LocalDateTime.of(date, LocalTime.of(datetime.getHour(), 0)).plusHours(1);
        } else if (rollingPeriod == RollingPeriod.MINUTELY) {
            // minutely
            nextDatetime = datetime.plusMinutes(1).withSecond(0);
        } else {
            // monthly
            nextDatetime = date.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        }
        return nextDatetime;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return "TimeBasedPolicy(namespacePattern=" + namespacePattern + ", rollingPeriod=" + rollingPeriod
                + ", namespace=" + namespace + ")";
    }

    public enum RollingPeriod {

        MONTHLY, DAILY, HOURLY, MINUTELY

    }

}
