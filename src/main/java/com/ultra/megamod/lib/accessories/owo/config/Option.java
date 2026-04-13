package com.ultra.megamod.lib.accessories.owo.config;

import java.util.function.Consumer;

/**
 * Adapter for io.wispforest.owo.config.Option.
 * Represents a single configurable option.
 */
public class Option<T> {

    private T value;
    private final Key key;

    public Option(Key key, T defaultValue) {
        this.key = key;
        this.value = defaultValue;
    }

    public T value() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public Key key() {
        return key;
    }

    public void observe(Consumer<T> observer) {
        // No-op in adapter
    }

    public record Key(String name) {
        public String asString() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    // Additional stubs for compatibility
    public String configName() { return "accessories"; }
    public boolean detached() { return false; }
}
