package com.ultra.megamod.lib.accessories.endec.adapter;

import com.mojang.serialization.Codec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructField;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Thin adapter for io.wispforest.endec.Endec. Wraps NeoForge Codec/StreamCodec
 * to provide the same interface pattern used by the Accessories mod.
 */
public interface Endec<T> {

    // Primitive endecs
    Endec<Boolean> BOOLEAN = ofCodec(Codec.BOOL);
    Endec<Integer> INT = ofCodec(Codec.INT);
    Endec<Integer> VAR_INT = ofCodec(Codec.INT);
    Endec<Long> LONG = ofCodec(Codec.LONG);
    Endec<Float> FLOAT = ofCodec(Codec.FLOAT);
    Endec<Double> DOUBLE = ofCodec(Codec.DOUBLE);
    Endec<String> STRING = ofCodec(Codec.STRING);
    Endec<byte[]> BYTES = new Endec<>() {
        @Override public Codec<byte[]> codec() { return Codec.BYTE_BUFFER.xmap(buf -> { byte[] arr = new byte[buf.remaining()]; buf.get(arr); return arr; }, java.nio.ByteBuffer::wrap); }
    };

    Codec<T> codec();

    default <U> Endec<U> xmap(Function<T, U> to, Function<U, T> from) {
        var self = this;
        return new Endec<>() {
            @Override public Codec<U> codec() { return self.codec().xmap(to::apply, from::apply); }
        };
    }

    default <U> Endec<U> xmapWithContext(java.util.function.BiFunction<SerializationContext, T, U> to, java.util.function.BiFunction<SerializationContext, U, T> from) {
        // Mojang Codec has no native concept of SerializationContext attributes,
        // so we thread the context via SerializationContext#current() which reads
        // from a ThreadLocal stack pushed by NbtMapCarrier/MapCarrierEncodable
        // sites before invoking Codec.parse / encodeStart.
        return xmap(t -> to.apply(SerializationContext.current(), t), u -> from.apply(SerializationContext.current(), u));
    }

    default Endec<List<T>> listOf() {
        var self = this;
        return new Endec<>() {
            @Override public Codec<List<T>> codec() { return self.codec().listOf(); }
        };
    }

    default Endec<Map<String, T>> mapOf() {
        var self = this;
        return new Endec<>() {
            @Override public Codec<Map<String, T>> codec() { return Codec.unboundedMap(Codec.STRING, self.codec()); }
        };
    }

    @SuppressWarnings("unchecked")
    default Endec<T> nullableOf() {
        return this; // Already nullable in practice
    }

    default Endec<Optional<T>> optionalOf() {
        var self = this;
        return new Endec<>() {
            @Override public Codec<Optional<T>> codec() {
                return self.codec().xmap(Optional::ofNullable, opt -> opt.orElse(null));
            }
        };
    }

    default Endec<T> validate(java.util.function.Consumer<T> validator) {
        return xmap(t -> { validator.accept(t); return t; }, t -> { validator.accept(t); return t; });
    }

    @SuppressWarnings("unchecked")
    default <S> StructField<S, T> fieldOf(String name, Function<S, T> getter) {
        return new StructField<>(name, this, getter, null);
    }

    default <S> StructField<S, T> optionalFieldOf(String name, Function<S, T> getter, T defaultValue) {
        return new StructField<>(name, this, getter, () -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    default <S> StructField<S, T> optionalFieldOf(String name, Function<S, T> getter, Supplier<T> defaultValue) {
        return new StructField<>(name, this, getter, defaultValue);
    }

    default KeyedEndec<T> keyed(String key, T defaultValue) {
        return new KeyedEndec<>(key, this, defaultValue);
    }

    default KeyedEndec<T> keyed(String key, Supplier<T> defaultValue) {
        return new KeyedEndec<>(key, this, defaultValue.get());
    }

    @SuppressWarnings("unchecked")
    default <C extends java.util.Collection<T>> Endec<C> collectionOf(Supplier<C> collectionFactory) {
        var self = this;
        return new Endec<>() {
            @Override public Codec<C> codec() {
                return self.codec().listOf().xmap(
                    list -> { C col = collectionFactory.get(); col.addAll(list); return col; },
                    col -> new java.util.ArrayList<>(col)
                );
            }
        };
    }

    @SuppressWarnings("unchecked")
    default Endec<java.util.Set<T>> setOf() {
        var self = this;
        return new Endec<>() {
            @Override public Codec<java.util.Set<T>> codec() {
                return self.codec().listOf().xmap(
                    list -> new java.util.LinkedHashSet<>(list),
                    set -> new java.util.ArrayList<>(set)
                );
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T> Endec<T> dispatchedStruct(Function<String, StructEndec<? extends T>> keyToEndec, Function<T, String> valueToKey, Endec<String> keyEndec, String keyFieldName) {
        // Dispatched struct: dynamically selects the right endec based on a key field
        return new Endec<>() {
            @Override
            public Codec<T> codec() {
                return Codec.STRING.dispatch(keyFieldName, valueToKey, key -> {
                    var endec = keyToEndec.apply(key);
                    return com.mojang.serialization.MapCodec.assumeMapUnsafe((Codec<? extends T>) endec.codec());
                });
            }
        };
    }

    static <T> Endec<T> of(java.util.function.BiConsumer<SerializationContext, T> encoder, Function<SerializationContext, T> decoder) {
        // Creates an endec from encode/decode functions - not directly supported, stub with empty codec
        return new Endec<>() {
            @Override
            public Codec<T> codec() {
                throw new UnsupportedOperationException("Endec.of(encoder, decoder) not backed by Codec");
            }
        };
    }

    // Static factory methods
    static <T> Endec<T> unit(java.util.function.Supplier<T> supplier) {
        return ofCodec(Codec.STRING.xmap(s -> supplier.get(), t -> ""));
    }

    @SuppressWarnings("unchecked")
    static <T> StructEndec<T> unit(T value) {
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                return Codec.STRING.xmap(s -> value, t -> "");
            }
        };
    }

    default StructEndec<T> structOf(String fieldName) {
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                return Endec.this.codec().fieldOf(fieldName).codec();
            }
        };
    }

    static <T> Endec<T> ofCodec(Codec<T> codec) {
        return new Endec<>() {
            @Override public Codec<T> codec() { return codec; }
        };
    }

    static <E extends Enum<E>> Endec<E> forEnum(Class<E> enumClass) {
        return ofCodec(Codec.STRING.xmap(
            name -> {
                for (E e : enumClass.getEnumConstants()) {
                    if (e.name().equalsIgnoreCase(name)) return e;
                }
                throw new IllegalArgumentException("Unknown enum value: " + name);
            },
            Enum::name
        ));
    }

    static <K, V> Endec<Map<K, V>> map(Endec<K> keyEndec, Endec<V> valueEndec) {
        return new Endec<>() {
            @Override
            public Codec<Map<K, V>> codec() {
                return Codec.unboundedMap(keyEndec.codec(), valueEndec.codec());
            }
        };
    }

    /**
     * Map endec with key conversion functions (used by EntitySlotLoader, RendererBindingLoader, etc.)
     */
    static <K, V> Endec<Map<K, V>> map(Function<K, String> keyToString, Function<String, K> stringToKey, Endec<V> valueEndec) {
        return new Endec<>() {
            @Override
            public Codec<Map<K, V>> codec() {
                Codec<K> keyCodec = Codec.STRING.xmap(stringToKey::apply, keyToString::apply);
                return Codec.unboundedMap(keyCodec, valueEndec.codec());
            }
        };
    }

    /**
     * Map endec with factory, key converters, and value endec (used by EntitySlotLoader)
     */
    @SuppressWarnings("unchecked")
    static <K, V, M extends Map<K, V>> Endec<M> map(java.util.function.Supplier<M> factory, Function<K, String> keyToString, Function<String, K> stringToKey, Endec<V> valueEndec) {
        return new Endec<>() {
            @Override
            public Codec<M> codec() {
                Codec<K> keyCodec = Codec.STRING.xmap(stringToKey::apply, keyToString::apply);
                return Codec.unboundedMap(keyCodec, valueEndec.codec()).xmap(
                    map -> { M m = factory.get(); m.putAll(map); return m; },
                    map -> map
                );
            }
        };
    }

    /**
     * Dispatched struct endec - selects StructEndec based on a key field extracted from the value.
     */
    @SuppressWarnings("unchecked")
    static <T, K> StructEndec<T> dispatched(Function<K, StructEndec<T>> keyToEndec, Function<T, K> valueToKey, Endec<K> keyEndec) {
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                // Use Codec.dispatch: extract key from value, then dispatch to the appropriate sub-codec.
                return keyEndec.codec().dispatch("id", valueToKey,
                    key -> {
                        var subEndec = keyToEndec.apply(key);
                        return com.mojang.serialization.MapCodec.assumeMapUnsafe(subEndec.codec());
                    });
            }
        };
    }

    static <T> AttributeEndec<T> ifAttr(SerializationAttribute attr, Endec<T> endec) {
        return new AttributeEndec<>(endec, attr);
    }

    // Encode/decode helpers - various overloads for compatibility
    default T decodeFully(SerializationContext ctx, Object deserializerFactory, Object input) {
        throw new UnsupportedOperationException("Direct decodeFully not supported in adapter - use Codec directly");
    }

    default Object encodeFully(SerializationContext ctx, Object serializerFactory, T value) {
        throw new UnsupportedOperationException("Direct encodeFully not supported in adapter - use Codec directly");
    }

    // Factory-style overloads used by RecordArgumentTypeInfo
    default <S> void encodeFully(java.util.function.Supplier<S> serializerFactory, T value) {
        // No-op - serialization handled via Codec
    }

    default <S> T decodeFully(java.util.function.Function<Object, S> deserializerFactory, Object input) {
        return null; // Stub
    }

    @SuppressWarnings("unchecked")
    default <S> Object encodeFully(java.util.function.Function<?, S> serializerFactory, T value) {
        return null; // Stub
    }

    /**
     * Wraps an Endec with attribute-conditional behavior
     */
    class AttributeEndec<T> implements Endec<T> {
        private final Endec<T> delegate;
        private final SerializationAttribute attribute;
        private Endec<T> fallback;

        public AttributeEndec(Endec<T> delegate, SerializationAttribute attribute) {
            this.delegate = delegate;
            this.attribute = attribute;
        }

        public Endec<T> orElse(Endec<T> fallback) {
            this.fallback = fallback;
            // In NeoForge context, we always use the primary endec (human-readable)
            return delegate;
        }

        @Override
        public Codec<T> codec() {
            return delegate.codec();
        }
    }
}
