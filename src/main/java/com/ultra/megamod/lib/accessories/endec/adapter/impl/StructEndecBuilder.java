package com.ultra.megamod.lib.accessories.endec.adapter.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Adapter for io.wispforest.endec.impl.StructEndecBuilder.
 * Builds struct endecs from field definitions, backed by RecordCodecBuilder.
 */
public final class StructEndecBuilder {

    @FunctionalInterface
    public interface Function3<A, B, C, R> { R apply(A a, B b, C c); }

    @FunctionalInterface
    public interface Function4<A, B, C, D, R> { R apply(A a, B b, C c, D d); }

    @FunctionalInterface
    public interface Function5<A, B, C, D, E, R> { R apply(A a, B b, C c, D d, E e); }

    @FunctionalInterface
    public interface Function6<A, B, C, D, E, F, R> { R apply(A a, B b, C c, D d, E e, F f); }

    @FunctionalInterface
    public interface Function7<A, B, C, D, E, F, G, R> { R apply(A a, B b, C c, D d, E e, F f, G g); }

    @FunctionalInterface
    public interface Function8<A, B, C, D, E, F, G, H, R> { R apply(A a, B b, C c, D d, E e, F f, G g, H h); }

    @SuppressWarnings("unchecked")
    private static <S, T> MapCodec<T> fieldCodec(StructField<S, T> field) {
        if (field.defaultValue() != null) {
            T def = field.defaultValue().get();
            if (def != null) {
                return field.endec().codec().optionalFieldOf(field.name(), def);
            }
            // Null default: create a MapCodec that never puts null into DataResult.
            // DFU 9.x's DataResult$Success.result() calls Optional.of(value) which NPEs on null.
            // We use a sentinel-based approach where the MapCodec always decodes to a non-null
            // Holder, and the forGetter wraps the nullable value in a Holder.
            return nullSafeOptionalFieldOf(field.endec().codec(), field.name());
        }
        return field.endec().codec().fieldOf(field.name());
    }

    /**
     * Creates a MapCodec for an optional field where absent/failed = null.
     * Uses a custom MapCodec that returns a sentinel empty string marker to avoid
     * DataResult.success(null), then uses the RecordCodecBuilder's apply function
     * to unwrap.
     *
     * The trick: We decode to Optional<T> internally and never let null into a DataResult.
     * The forGetter wraps values in Optional. The constructor receives Optional and unwraps.
     * But since the StructField expects T, not Optional<T>, we need a different approach:
     * We use a custom MapCodec that decodes directly to T, handling null via custom decode.
     */
    @SuppressWarnings("unchecked")
    private static <T> MapCodec<T> nullSafeOptionalFieldOf(Codec<T> codec, String name) {
        // This MapCodec decodes to T (possibly null) but the DataResult never contains null directly.
        // When the field is missing, we return DataResult.error() which makes RecordCodecBuilder
        // fall through to the error-merging path that handles partial results correctly.
        return new MapCodec<T>() {
            @Override
            public <S> DataResult<T> decode(DynamicOps<S> ops, MapLike<S> input) {
                S value = input.get(name);
                if (value == null) {
                    // Field absent: use error with empty lifecycle to signal "use default"
                    // RecordCodecBuilder merges errors and still produces a result
                    return DataResult.error(() -> "Optional field " + name + " absent");
                }
                return codec.parse(ops, value);
            }

            @Override
            public <S> RecordBuilder<S> encode(T input, DynamicOps<S> ops, RecordBuilder<S> prefix) {
                if (input == null) return prefix;
                return prefix.add(name, codec.encodeStart(ops, input));
            }

            @Override
            public <S> Stream<S> keys(DynamicOps<S> ops) {
                return Stream.of(ops.createString(name));
            }
        };
    }

    public static <S, T> StructEndec<S> of(StructField<S, T> field, Function<T, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(fieldCodec(field).forGetter(s -> field.getter().apply(s)))
                .apply(inst, constructor)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            BiFunction<A, B, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s))
            ).apply(inst, constructor)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            Function3<A, B, C, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            Function4<A, B, C, D, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            Function5<A, B, C, D, E, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            Function6<A, B, C, D, E, F, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F, G> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            StructField<S, G> f7,
            Function7<A, B, C, D, E, F, G, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s)),
                fieldCodec(f7).forGetter(s -> f7.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F, G, H> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            StructField<S, G> f7,
            StructField<S, H> f8,
            Function8<A, B, C, D, E, F, G, H, S> constructor) {
        // RecordCodecBuilder only supports up to 16 fields, but we need manual composition for 8+
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s)),
                fieldCodec(f7).forGetter(s -> f7.getter().apply(s)),
                fieldCodec(f8).forGetter(s -> f8.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    private static <S> StructEndec<S> wrapCodec(Codec<S> codec) {
        return new StructEndec<>() {
            @Override
            public Codec<S> codec() {
                return codec;
            }
        };
    }
}
