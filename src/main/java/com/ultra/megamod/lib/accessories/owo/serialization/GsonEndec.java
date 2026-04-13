package com.ultra.megamod.lib.accessories.owo.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;

/**
 * Adapter for io.wispforest.owo.serialization.format.gson.GsonEndec.
 * An Endec<JsonElement> that uses Mojang's JsonOps for serialization.
 */
public final class GsonEndec implements Endec<JsonElement> {

    public static final GsonEndec INSTANCE = new GsonEndec();

    private GsonEndec() {}

    @Override
    public Codec<JsonElement> codec() {
        // A codec that serializes JsonElement passthrough
        return Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            jsonElement -> new com.mojang.serialization.Dynamic<>(JsonOps.INSTANCE, jsonElement)
        );
    }
}
