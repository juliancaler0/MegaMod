package com.ultra.megamod.lib.accessories.endec.adapter.format.gson;

import com.google.gson.JsonObject;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierDecodable;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for io.wispforest.endec.format.gson.GsonMapCarrier.
 * Stub wrapper around a JsonObject for map-carrier pattern.
 */
public record GsonMapCarrier(JsonObject object) implements MapCarrierEncodable, MapCarrierDecodable {
    public GsonMapCarrier() {
        this(new JsonObject());
    }

    @Override
    public <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        // Stub - JSON encoding not critical for this adapter
    }

    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        return key.defaultValue();
    }

    @Override
    public <T> boolean has(@NotNull KeyedEndec<T> key) {
        return object.has(key.key());
    }
}
