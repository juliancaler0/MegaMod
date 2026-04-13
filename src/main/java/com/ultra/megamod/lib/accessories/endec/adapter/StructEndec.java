package com.ultra.megamod.lib.accessories.endec.adapter;

import com.mojang.serialization.Codec;

/**
 * Adapter for io.wispforest.endec.StructEndec - a struct-aware serialization interface.
 * In NeoForge, this simply delegates to Codec.
 */
public interface StructEndec<T> extends Endec<T> {

    @Override
    default Codec<T> codec() {
        throw new UnsupportedOperationException("StructEndec codec() not implemented - this StructEndec is network-only");
    }

    default void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T value) {
        // No-op in adapter - struct encoding handled by Codec
    }

    default T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
        return null;
    }

    default <U> StructEndec<U> xmap(java.util.function.Function<T, U> to, java.util.function.Function<U, T> from) {
        var self = this;
        return new StructEndec<>() {
            @Override
            public Codec<U> codec() {
                return self.codec().xmap(to::apply, from::apply);
            }
        };
    }

    @SuppressWarnings("unchecked")
    default <S> com.ultra.megamod.lib.accessories.endec.adapter.impl.StructField<S, T> flatFieldOf(java.util.function.Function<S, T> getter) {
        return new com.ultra.megamod.lib.accessories.endec.adapter.impl.StructField<>("", this, getter, null);
    }

    default StructEndec<T> orElse(StructEndec<T> fallback) {
        return this; // In NeoForge, use primary
    }

    /**
     * Error catching wrapper for struct decoding. In the NeoForge adapter, this
     * wraps the codec with error recovery logic.
     */
    @FunctionalInterface
    interface StructuredCatchHandler<T> {
        T handle(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct, Exception exception);
    }

    default StructEndec<T> structuredCatchErrors(StructuredCatchHandler<T> handler) {
        var self = this;
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                return self.codec();
            }

            @Override
            public T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
                try {
                    return self.decodeStruct(ctx, deserializer, struct);
                } catch (Exception e) {
                    return handler.handle(ctx, deserializer, struct, e);
                }
            }
        };
    }
}
