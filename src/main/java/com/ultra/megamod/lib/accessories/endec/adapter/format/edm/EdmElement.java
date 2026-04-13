package com.ultra.megamod.lib.accessories.endec.adapter.format.edm;

/**
 * Adapter for io.wispforest.endec.format.edm.EdmElement.
 * EDM (Endec Data Model) element - stub for compatibility.
 */
public final class EdmElement<T> {
    private final T value;

    private EdmElement(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <V> java.util.Map<String, EdmElement<?>> asMap() {
        return (java.util.Map<String, EdmElement<?>>) value;
    }

    public static <T> EdmElement<T> of(T value) {
        return new EdmElement<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends java.util.Map<?, ?>> EdmElement<T> consumeMap(T map) {
        return new EdmElement<>(map);
    }
}
