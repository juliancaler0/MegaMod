package com.ultra.megamod.lib.accessories.endec.adapter.util;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for io.wispforest.endec.util.MapCarrierDecodable.
 */
public interface MapCarrierDecodable {

    <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key);

    <T> boolean has(@NotNull KeyedEndec<T> key);

    default <T> T get(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        try {
            return getWithErrors(ctx, key);
        } catch (Exception e) {
            return key.defaultValue();
        }
    }

    default <T> T get(@NotNull KeyedEndec<T> key) {
        return get(SerializationContext.empty(), key);
    }
}
