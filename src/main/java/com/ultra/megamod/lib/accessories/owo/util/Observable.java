package com.ultra.megamod.lib.accessories.owo.util;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.util.Observable.
 */
public class Observable<T> {
    private T value;

    private Observable(T value) {
        this.value = value;
    }

    public static <T> Observable<T> of(T value) {
        return new Observable<>(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void observe(Consumer<T> observer) {
        // Stub - would normally trigger observer on changes
    }
}
