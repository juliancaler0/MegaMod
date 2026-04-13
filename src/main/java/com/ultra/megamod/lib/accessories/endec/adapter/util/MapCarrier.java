package com.ultra.megamod.lib.accessories.endec.adapter.util;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for io.wispforest.endec.util.MapCarrier.
 * Provides map-like get/put/delete/has operations for serialization carriers.
 */
public interface MapCarrier extends MapCarrierDecodable, MapCarrierEncodable {

    @Override
    <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key);

    @Override
    <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value);

    <T> void delete(@NotNull KeyedEndec<T> key);

    @Override
    <T> boolean has(@NotNull KeyedEndec<T> key);

    default <T> T get(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        try {
            return getWithErrors(ctx, key);
        } catch (Exception e) {
            return key.defaultValue();
        }
    }
}
