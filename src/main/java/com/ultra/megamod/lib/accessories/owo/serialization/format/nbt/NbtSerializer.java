package com.ultra.megamod.lib.accessories.owo.serialization.format.nbt;

/**
 * Adapter for io.wispforest.owo.serialization.format.nbt.NbtSerializer.
 * In NeoForge, NBT serialization uses Codec directly.
 */
public final class NbtSerializer {
    private NbtSerializer() {}

    public static NbtSerializer of(Object ignored) {
        return new NbtSerializer();
    }
}
