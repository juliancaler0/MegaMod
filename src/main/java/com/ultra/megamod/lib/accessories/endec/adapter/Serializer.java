package com.ultra.megamod.lib.accessories.endec.adapter;

/**
 * Adapter for io.wispforest.endec.Serializer.
 * In NeoForge, serialization is handled by Codec/StreamCodec, so this is a thin shim.
 */
public interface Serializer<T> {

    interface Struct {
        default <V> void field(String name, SerializationContext ctx, Endec<V> endec, V value) {
            // No-op in NeoForge adapter
        }
    }
}
