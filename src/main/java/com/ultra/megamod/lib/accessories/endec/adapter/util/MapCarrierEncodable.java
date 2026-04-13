package com.ultra.megamod.lib.accessories.endec.adapter.util;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter for io.wispforest.endec.util.MapCarrierEncodable.
 */
public interface MapCarrierEncodable {

    <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value);

    default <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        put(SerializationContext.empty(), key, value);
    }

    default <T> void putIfNotNull(SerializationContext ctx, @NotNull KeyedEndec<T> key, @Nullable T value) {
        if (value != null) {
            put(ctx, key, value);
        }
    }

    default <T> void putIfNotNull(@NotNull KeyedEndec<T> key, @Nullable T value) {
        putIfNotNull(SerializationContext.empty(), key, value);
    }

    default <T> void delete(@NotNull KeyedEndec<T> key) {
        // No-op default
    }
}
