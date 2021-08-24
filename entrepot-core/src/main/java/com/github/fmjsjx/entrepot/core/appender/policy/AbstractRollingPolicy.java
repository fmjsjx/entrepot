package com.github.fmjsjx.entrepot.core.appender.policy;

import java.util.Objects;

import com.github.fmjsjx.entrepot.core.appender.RollingPolicy;

public abstract class AbstractRollingPolicy implements RollingPolicy {

    @Override
    public boolean canTriggered(TriggeringPhase phase) {
        Objects.requireNonNull(phase, "phase must not be null");
        if (phase == TriggeringPhase.BEFORE_APPEND) {
            return canTriggeredBeforeAppend();
        } else {
            return canTriggeredAfterAppend();
        }
    }

    protected boolean canTriggeredBeforeAppend() {
        return true;
    }

    protected boolean canTriggeredAfterAppend() {
        return false;
    }

}
