package com.ultra.megamod.lib.accessories.fabric.event;

import java.util.function.Function;

/**
 * Adapter for net.fabricmc.fabric.api.event.EventFactory.
 * Creates Event instances with the given invoker factory.
 */
public final class EventFactory {

    private EventFactory() {}

    public static <T> Event<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
        return new Event<>(listeners -> {
            @SuppressWarnings("unchecked")
            T[] array = listeners.toArray((T[]) java.lang.reflect.Array.newInstance(type, 0));
            return invokerFactory.apply(array);
        });
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, T defaultInvoker, Function<T[], T> invokerFactory) {
        return new Event<>(listeners -> {
            if (listeners.isEmpty()) return defaultInvoker;
            @SuppressWarnings("unchecked")
            T[] array = listeners.toArray((T[]) java.lang.reflect.Array.newInstance(type, 0));
            return invokerFactory.apply(array);
        });
    }
}
