package com.github.fmjsjx.entrepot.core.wharf;

import java.util.Optional;
import java.util.function.Function;

import com.github.fmjsjx.entrepot.core.Wharf;

public interface Wharves extends Function<String, Optional<Wharf>> {

    @Override
    default Optional<Wharf> apply(String t) {
        return get(t);
    }

    Optional<Wharf> get(String name);

    /**
     * Clear and close all {@link Wharf}.
     */
    void clear();

}
