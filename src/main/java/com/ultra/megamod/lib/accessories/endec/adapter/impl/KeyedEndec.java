package com.ultra.megamod.lib.accessories.endec.adapter.impl;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;

/**
 * Adapter for io.wispforest.endec.impl.KeyedEndec.
 * Represents a keyed field with a default value for map-based serialization.
 */
public class KeyedEndec<T> {
    private final String key;
    private final Endec<T> endec;
    private final T defaultValue;

    public KeyedEndec(String key, Endec<T> endec, T defaultValue) {
        this.key = key;
        this.endec = endec;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public Endec<T> endec() {
        return endec;
    }

    public T defaultValue() {
        return defaultValue;
    }
}
