package com.github.fmjsjx.entrepot.core.appender;

import java.util.Optional;

public interface RollingPolicy {

    enum TriggeringPhase {

        BEFORE_APPEND,

        AFTER_APPEND

    }

    boolean canTriggered(TriggeringPhase phase);

    default boolean update(final long size) {
        return update(System.currentTimeMillis(), size);
    }

    boolean update(final long timeMillis, final long size);

    default Optional<String> tryUpdate(final long size) {
        return tryUpdate(System.currentTimeMillis(), size);
    }

    default Optional<String> tryUpdate(final long timeMillis, final long size) {
        if (update(timeMillis, size)) {
            return Optional.of(namespace());
        }
        return Optional.empty();
    }

    String namespace();

}
