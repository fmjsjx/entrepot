package com.github.fmjsjx.entrepot.core.wharf;

import java.util.function.Function;

import com.github.fmjsjx.entrepot.core.Wharf;

@FunctionalInterface
public interface WharfFactory extends Function<String, Wharf> {

    @Override
    default Wharf apply(String t) {
        return create(t);
    }

    Wharf create(String name);

}
