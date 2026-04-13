package com.ultra.megamod.lib.accessories.endec.adapter.impl;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Adapter for io.wispforest.endec.impl.StructField.
 * Represents a named field in a struct endec with getter and default value.
 */
public class StructField<S, T> {
    private final String name;
    private final Endec<T> endec;
    private final Function<S, T> getter;
    private final Supplier<T> defaultValue;

    public StructField(String name, Endec<T> endec, Function<S, T> getter, Supplier<T> defaultValue) {
        this.name = name;
        this.endec = endec;
        this.getter = getter;
        this.defaultValue = defaultValue;
    }

    public String name() { return name; }
    public Endec<T> endec() { return endec; }
    public Function<S, T> getter() { return getter; }
    public Supplier<T> defaultValue() { return defaultValue; }
}
