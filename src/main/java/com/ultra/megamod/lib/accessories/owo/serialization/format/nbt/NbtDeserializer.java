package com.ultra.megamod.lib.accessories.owo.serialization.format.nbt;

/**
 * Adapter for io.wispforest.owo.serialization.format.nbt.NbtDeserializer.
 * In NeoForge, NBT deserialization uses Codec directly.
 */
public final class NbtDeserializer {
    private NbtDeserializer() {}

    public static NbtDeserializer of(Object ignored) {
        return new NbtDeserializer();
    }
}
