package com.ultra.megamod.lib.accessories.owo.util;

/**
 * Adapter for io.wispforest.owo.util.EventSource.
 * Read-only view of an EventStream for subscribing listeners.
 */
public interface EventSource<T> {

    EventStream.EventSubscription subscribe(int priority, T listener);

    default EventStream.EventSubscription subscribe(T listener) {
        return subscribe(0, listener);
    }
}
