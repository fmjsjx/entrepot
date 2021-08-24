package com.github.fmjsjx.entrepot.core.wharf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.fmjsjx.entrepot.core.Wharf;

import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
public class DynamicWharves implements Wharves {

    @ToString.Include
    private final WharfFactory factory;
    @ToString.Include
    private final ConcurrentMap<String, Optional<Wharf>> wharves = new ConcurrentHashMap<>();

    public DynamicWharves(WharfFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    public DynamicWharves(WharfFactory factory, String... prepareNames) {
        this(factory, Arrays.asList(prepareNames));
    }

    public DynamicWharves(WharfFactory factory, List<String> prepareNames) {
        this(factory);
        prepareNames.stream().distinct().map(factory::create).forEach(this::putHangar);
    }

    private void putHangar(Wharf hangar) {
        wharves.put(hangar.name(), Optional.of(hangar));
    }

    @Override
    public Optional<Wharf> get(String name) {
        return wharves.computeIfAbsent(name, this::createHangar);
    }

    private Optional<Wharf> createHangar(String name) {
        return Optional.of(factory.create(name));
    }

    @Override
    public void clear() {
        wharves.values().stream().map(Optional::get).forEach(Wharf::close);
        wharves.clear();
    }

}
