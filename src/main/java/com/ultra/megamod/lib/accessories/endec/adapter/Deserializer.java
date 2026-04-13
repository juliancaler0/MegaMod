package com.ultra.megamod.lib.accessories.endec.adapter;

/**
 * Adapter for io.wispforest.endec.Deserializer.
 * In NeoForge, deserialization is handled by Codec/StreamCodec, so this is a thin shim.
 */
public interface Deserializer<T> {

    interface Struct {
        default <V> V field(String name, SerializationContext ctx, Endec<V> endec) {
            return null; // Stub in NeoForge adapter
        }
    }
}
