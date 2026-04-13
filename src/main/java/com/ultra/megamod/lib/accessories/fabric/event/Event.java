package com.ultra.megamod.lib.accessories.fabric.event;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Adapter for net.fabricmc.fabric.api.event.Event.
 * Simple event bus that collects listeners and provides an invoker.
 */
public class Event<T> {

    private final List<T> listeners = new CopyOnWriteArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    protected T invoker;

    protected Event() {
        this.invokerFactory = null;
        this.invoker = null;
    }

    Event(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.invoker = invokerFactory.apply(List.of());
    }

    public void register(T listener) {
        listeners.add(listener);
        if (invokerFactory != null) {
            invoker = invokerFactory.apply(List.copyOf(listeners));
        }
    }

    public void register(Identifier phase, T listener) {
        // Phase-aware registration - just delegates to normal register in this adapter
        register(listener);
    }

    public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
        // Phase ordering - no-op in adapter
    }

    public T invoker() {
        return invoker;
    }
}
