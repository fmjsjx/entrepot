package com.github.fmjsjx.entrepot.core.wharf;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.fmjsjx.entrepot.core.Wharf;

import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
public class StaticWharves implements Wharves {

    @ToString.Include
    private final Map<String, Optional<Wharf>> wharves;

    public StaticWharves(List<Wharf> hangars) {
        Objects.requireNonNull(hangars, "hangars must not be null");
        this.wharves = hangars.stream().collect(Collectors.toUnmodifiableMap(Wharf::name, Optional::of));
    }

    public StaticWharves(Wharf... hangars) {
        this(Arrays.asList(hangars));
    }

    @Override
    public Optional<Wharf> get(String name) {
        return wharves.getOrDefault(name, Optional.empty());
    }

    @Override
    public void clear() {
        wharves.values().stream().map(Optional::get).forEach(Wharf::close);
        wharves.clear();
    }

}
