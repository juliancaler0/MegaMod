package com.ultra.megamod.lib.accessories.owo.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Adapter for io.wispforest.owo.util.EventStream.
 * A simple event bus that collects listeners and invokes them through an invoker factory.
 */
public class EventStream<T> implements EventSource<T> {

    protected final List<T> subscribers = new CopyOnWriteArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    private T invoker;

    public EventStream(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.invoker = invokerFactory.apply(List.of());
    }

    public EventSubscription subscribe(T listener) {
        subscribers.add(listener);
        invoker = invokerFactory.apply(List.copyOf(subscribers));
        return () -> {
            subscribers.remove(listener);
            invoker = invokerFactory.apply(List.copyOf(subscribers));
        };
    }

    public T invoker() {
        return invoker;
    }

    protected void addSubscriber(T subscriber) {
        subscribers.add(subscriber);
        invoker = invokerFactory.apply(List.copyOf(subscribers));
    }

    protected void removeSubscriber(T subscriber) {
        subscribers.remove(subscriber);
        invoker = invokerFactory.apply(List.copyOf(subscribers));
    }

    /**
     * Returns this EventStream as an EventSource for subscribing.
     * Mirrors the owo-lib source() API.
     */
    public EventSource<T> source() {
        return this;
    }

    /**
     * Returns the current invoker for dispatching events.
     * Mirrors the owo-lib sink() API.
     */
    public T sink() {
        return invoker;
    }

    @Override
    public EventSubscription subscribe(int priority, T listener) {
        subscribers.add(listener);
        invoker = invokerFactory.apply(List.copyOf(subscribers));
        return () -> {
            subscribers.remove(listener);
            invoker = invokerFactory.apply(List.copyOf(subscribers));
        };
    }

    public interface EventSubscription {
        void unsubscribe();

        /**
         * Alias for unsubscribe(), for compatibility with code using cancel().
         */
        default void cancel() {
            unsubscribe();
        }
    }
}
